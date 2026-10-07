const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can read and write own user document", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      displayName: "Alice",
      activeProfileId: "default",
      lifetimeSteps: 1000,
      lifetimeXp: 50,
      currentStreak: 2,
      bestStreak: 5,
      createdAt: now,
      updatedAt: now,
    })
  );

  const doc = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user access: Bob cannot read or write Alice user document", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const now = new Date();
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      displayName: "Alice",
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: new Date(),
    })
  );
});

test("Profiles subcollection: Owner can create and read profiles, other user cannot", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("profiles").doc("runner").set({
      profileId: "runner",
      userId: ALICE_UID,
      name: "Runner Mode",
      emoji: "🏃",
      colorHex: 0,
      dailySteps: 500,
      bankedSeconds: 120,
      stepsPerMinute: 100,
      dailyStepGoal: 5000,
      bonusMinutes: 10,
      createdAt: now,
      updatedAt: now,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("profiles").doc("runner").get()
  );
});

test("History subcollection: Owner can record day history, other user cannot", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("history").doc("2026-10-07").set({
      dateIso: "2026-10-07",
      userId: ALICE_UID,
      dayLabel: "Wed",
      steps: 6500,
      earnedMinutes: 65,
      spentMinutes: 30,
      goalReached: true,
      updatedAt: now,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("history").doc("2026-10-07").get()
  );
});
