/// <reference path='../node_modules/mocha-typescript/globals.d.ts' />
import * as firebase from "@firebase/testing";
import * as fs from "fs";

/*
 * ============
 *    Setup
 * ============
 */
const projectId = "firestore-emulator-example";
const coverageUrl = `http://localhost:8080/emulator/v1/projects/${projectId}:ruleCoverage.html`;

const rules = fs.readFileSync("firestore.rules", "utf8");

/**
 * Creates a new app with authentication data matching the input.
 *
 * @param {object} auth the object to use for authentication (typically {uid: some-uid})
 * @return {object} the app.
 */
function authedApp(auth) {
  return firebase
    .initializeTestApp({ projectId, auth })
    .firestore();
}

function adminApp() {
  return firebase
    .initializeAdminApp({ projectId })
    .firestore();
}

/*
 * ============
 *  Test Cases
 * ============
 */
before(async () => {
  await firebase.loadFirestoreRules({ projectId, rules });
});

beforeEach(async () => {
  // Clear the database between tests
  await firebase.clearFirestoreData({ projectId });
  const db = adminApp();
  const doc = db.collection("users").doc("alice");

  await doc.set({
    name: "alice",
  });

  const instanceIdDoc = doc.collection("instanceIds").doc("id");
  await instanceIdDoc.set({
    token: "token",
    languageTag: "fr"
  });
});

after(async () => {
  await Promise.all(firebase.apps().map(app => app.delete()));
  console.log(`View rule coverage information at ${coverageUrl}\n`);
});

@suite
class MyApp {

  @test
  async "should not let anyone create any document"() {
    const db = authedApp(null);
    const testDoc = db.collection("anyCollection").doc("any");
    await firebase.assertFails(
      testDoc.set({
        owner: "alice",
        topic: "All Things Firebase"
      })
    );
  }

  @test
  async "should not let anyone read other profile"() {
    const db = authedApp(null);
    const profile = db.collection("users").doc("alice");
    await firebase.assertFails(profile.get());
  }

  @test
  async "should not let user read other profile"() {
    const db = authedApp({ uid: "alice" });
    const profile = db.collection("users").doc("test");
    await firebase.assertFails(profile.get());
  }

  @test
  async "should let user read their own profile"() {
    const db = authedApp({ uid: "alice" });
    const profile = db.collection("users").doc("alice");
    await firebase.assertSucceeds(profile.get());
  }

  @test
  async "should not let anyone create a users"() {
    const db = authedApp(null);
    const doc = db.collection("users").doc("uid");
    await firebase.assertFails(
      doc.set({
        owner: "alice",
        topic: "All Things Firebase"
      })
    );
  }

  @test
  async "should not let user create a their user"() {
    const db = authedApp({ uid: "alice" });
    const doc = db.collection("users").doc("alice");
    await firebase.assertFails(
      doc.set({
        owner: "alice",
        topic: "All Things Firebase"
      })
    );
  }

  @test
  async "should let user create their own instanceIds"() {
    const db = authedApp({ uid: "alice" });
    const doc = db.collection("users/alice/instanceIds").doc("id2");
    await firebase.assertSucceeds(
      doc.set({
        token: "token",
        languageTag: "fr"
      })
    );
  }

  @test
  async "should let user update their own instanceIds"() {
    const db = authedApp({ uid: "alice" });
    const doc = db.collection("users/alice/instanceIds").doc("id");
    await firebase.assertSucceeds(
      doc.update({
        token: "newToken"
      })
    );
  }

  @test
  async "should not let user create others instanceIds"() {
    const db = authedApp({ uid: "alice" });
    const doc = db.collection("users/toto/instanceIds").doc("id");
    await firebase.assertFails(
      doc.set({
      token: "token",
      languageTag: "fr"
      })
    );
  }
}
