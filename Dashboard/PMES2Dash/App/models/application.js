/**
 * Created by bscuser on 9/13/16.
 */
var mongoose = require('mongoose');

var appSchema = mongoose.Schema({
    name : String,
    image: String,
    location: String,
    path: String,
    executable: String,
    description: String,
    compss: Boolean,
    publicApp: Boolean,
    user: String,
    args: [{name: String, defaultV: String, prefix: String, file: Boolean, optional: Boolean}]

});

module.exports = mongoose.model('Application', appSchema);