var exec = require('cordova/exec');

module.exports = {
  openCamera: function(success, error) {
    exec(success, error, "UVCCamera", "openCamera", []);
  },
  takePicture: function(success, error) {
    exec(success, error, "UVCCamera", "takePicture", [!!resultType]);
  }
};