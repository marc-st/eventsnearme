const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.recordGender = functions.database.ref('users')
  .onCreate((snapshot, context) => {
    const gender = snapshot.child('gender').val()
    if (gender.trim() === 'Male'){
      var male = snapshot.ref.root.child('meta/Male')
      return male.transaction((count) => {return count+1})
    }
    else {
      var female = snapshot.ref.root.child('meta/Female')
      return female.transaction((count) => {return count+1})
    }
});
