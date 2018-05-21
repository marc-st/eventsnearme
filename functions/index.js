const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.recordGender = functions.database.ref('users')
  .onCreate((snapshot, context) => {
    const gender = snapshot.child('gender').val()
    const reference = functions.database.ref('meta')
    if (gender === 'Male')
      return reference.child('Male').set(reference.child('Male').val() + 1)
    else
      return reference.child('Female').set(reference.child('Female').val() + 1)
});
