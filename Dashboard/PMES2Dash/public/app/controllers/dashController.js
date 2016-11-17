/**
 * Created by bscuser on 9/14/16.
 */
'use strict';

angular.module('pmes2')
    .controller('dashController', ['$state', '$http', function($state, $http) {
        var store = this;
        //TODO: clean user variables
        store.user = {
            username: "scorella",
            credentials: {
                key: "/home/pmes/certs/scorella_test.key",
                pem: "/home/pmes/certs/scorella_test.pem"
            }
        };
        store.userS = {
            _id: "57f601522aa80824dc4f094e"
        };

        this.newApp = {args:[], user:"test"};
        this.infoApp = {};

        this.arg = {};

        this.newJob = {};
        this.newJob.app = {};
        this.newJob.img = {};
        this.infoJob = {};

        this.newStorage = {};
        this.infoStorage = {};

        this.jobsList = [];
        this.appsList = [];
        this.storagesList = [];
        this.imagesList = [];
        this.usersList = [];

        this.error = null;
        this.logFile ="";
        this.readFile = "";
        this.logFiles = ["log", "out", "err"];
        this.logData ="";

        this.selectedJob = {};
        this.selectedJobs = [];

        //TODO: remove groups list from variables
        this.groups = [];
        this.Groups = ["test", "all"];
        this.getLog = function(){
            if(store.logFile == "log"){
                store.readFile = {file:"/home/pmes/pmes/logs/log.txt"};
            } else if(store.logFile == "err"){
                store.readFile = {file:"/home/pmes/pmes/logs/err.txt"};
            } else {
                store.readFile = {file:"/home/pmes/pmes/logs/out.txt"};
            }
            $http({
                method: 'POST',
                url: 'dash/log',
                data: this.readFile
            }).then(
                function(data) {
                    store.logData = data.data;
                    store.error = null;
                    document.getElementById("logData").value = store.logData;
                },
                function(error) {
                    store.logData = "Error";
                    store.error = 'HA FALLADO: '+error.data.error;
                    document.getElementById("logData").value = store.logData;
                }
            );

        };

        this.saveNewApp = function() {
            this.newApp.user = store.userS._id;
            this.newApp.location = store.newApp.location._id;
            $http({
                method: 'POST',
                url: 'dash/app',
                data: this.newApp
            }).then(
                function(data) {
                    $('#newApp').modal('hide');
                    store.appsList.push(store.newApp);
                    store.error = null;
                    store.newApp = null;
                },
                function(error) {
                    store.error = 'ERROR: '+error.data.error;
                }
            );
        };

        this.saveNewJob = function() {
            this.newJob.app = this.newJob.appVal;
            this.newJob.jobName = this.newJob.appVal.name;
            this.newJob.status = 'created';
            this.newJob.user = store.userS._id;
            this.newJob.img.imageName = this.newJob.appVal.image;
            this.newJob.img.imageType = 'small';
            this.newJob.submitted = new Date().toJSON().slice(0,10);
            this.newJob.finished = new Date().toJSON().slice(0,10);
            this.newJob.duration = 0;
            this.newJob.errorMessage = "";
            this.newJob.numNodes = "1";
            this.newJob.initialVMs = this.newJob.minimumVMs;
            this.newJob.limitVMs = this.newJob.maximumVMs;
            this.newJob.log = "";
            this.newJob.output = "";
            this.newJob.error = "";
            this.newJob.inputPath = "/home/";
            this.newJob.outputPath = "/home/";

            $http({
                method: 'POST',
                url: 'dash/job',
                data: this.newJob
            }).then(
                function(data) {
                    $('#newJob').modal('hide');
                    store.newJob.jobName = data.data;
                    store.jobsList.push(store.newJob);
                    store.error = null;
                    store.newJob = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                    store.newJob = null;
                }
            );

        };

        this.runJob = function(job){
            var jobToSend = this.formatJob(job);
            $http({
                method: 'POST',
                url: 'api/createActivity',
                data: jobToSend
            }).then(
                function(data) {
                    store.error = null;
                    store.getJobsList();
                }, function(error) {
                    store.error = 'Activity creation error'+error.data.error;
                }
            );
        };

        this.stopJob = function(job){
            //TODO
        };

        this.removeJob = function(job) {
            $http({
                method: 'DELETE',
                url: 'dash/job',
                data: job,
                headers: {"Content-Type": "application/json;charset=utf-8"}
            }).then(
                function(data) {
                    store.error = null;
                }, function(error) {
                    store.error = 'Job deletion error '+error.data.error;
                }
            );
            var index = this.jobsList.indexOf(job);
            if (index > -1){
                this.jobsList.splice(index, 1);
            }
        };

        this.refreshJobs = function () {
            var jobIds = this.selectedJobs;
            console.log(jobIds);
            $http({
                method: 'POST',
                url: 'api/getActivityStatus',
                data: jobIds
            }).then(
                function(data) {
                    store.error = null;
                    store.getJobsList();
                }, function(error) {
                    store.error = 'Get Activity status error '+error.data.error;
                }
            );
        };

        this.removeStorage = function(storage) {
            $http({
                method: 'DELETE',
                url: 'dash/storage',
                data: storage,
                headers: {"Content-Type": "application/json;charset=utf-8"}
            }).then(
                function(data) {
                    store.error = null;
                }, function(error) {
                    store.error = 'storage deletion error '+error.data.error;
                }
            );
            var index = store.storagesList.indexOf(storage);
            if (index > -1){
                store.storagesList.splice(index, 1);
            }
        };

        this.removeApp = function(app) {
            $http({
                method: 'DELETE',
                url: 'dash/app',
                data: app,
                headers: {"Content-Type": "application/json;charset=utf-8"}
            }).then(
                function(data) {
                    store.error = null;
                }, function(error) {
                    store.error = 'app deletion error '+error.data.error;
                }
            );
            var index = store.appsList.indexOf(app);
            if (index > -1){
                store.appsList.splice(index, 1);
            }
        };

        this.removeUser = function(user) {
            $http({
                method: 'DELETE',
                url: 'dash/user',
                data: user,
                headers: {"Content-Type": "application/json;charset=utf-8"}
            }).then(
                function(data) {
                    store.error = null;
                }, function(error) {
                    store.error = 'user deletion error '+error.data.error;
                }
            );
            var index = store.usersList.indexOf(user);
            if (index > -1){
                store.usersList.splice(index, 1);
            }
        };

        this.formatJob = function(job) {
            var resultArgs = job.app.args.reduce(function(map, obj){
                map[obj.name] = obj.value;
                return map;
            }, {});
            var jobToSend = {
                "jobName":job.jobName,
                "wallTime": job.wallTime,
                "minimumVMs": job.minimumVMs,
                "maximumVMs": job.maximumVMs,
                "limitVMs": job.maximumVMs,
                "initialVMs": job.minimumVMs,
                "memory": job.memory,
                "cores": job.cores,
                "inputPath": job.inputPath,
                "outputPath": job.outputPath,
                "numNodes": job.numNodes,
                "user": {
                    "username": store.user.username,
                    "credentials": store.user.credentials
                },
                "img": job.img,
                "app": {
                    "name":job.app.name,
                    "target":job.app.target,
                    "source":job.app.source,
                    "args": resultArgs
                }
            };
            return jobToSend;
        };


        this.saveNewJobAndRun = function() {
            store.saveNewJob();
            this.runJob(store.jobsList[store.jobsList.length-1]);

            //this.runJob(store.newJob);
        };

        this.saveNewStorage = function() {
            $http({
                method: 'POST',
                url: 'dash/storage',
                data: this.newStorage
            }).then(
                function(data) {
                    $('#newStorage').modal('hide');
                    store.storagesList.push(store.newStorage);
                    store.error = null;
                    store.newStorage = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                }
            );
        };

        this.updateStorage = function(){
            $http({
                method: 'PUT',
                url: 'dash/storage',
                data: this.infoStorage
            }).then(
                function(data) {
                    store.storagesList[store.index]= store.infoStorage;
                    store.error = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                }
            );
        };

        this.updateApp = function(){
            $http({
                method: 'PUT',
                url: 'dash/app',
                data: this.infoApp
            }).then(
                function(data) {
                    store.appsList[store.index]= store.infoApp;
                    store.error = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                }
            );
        };
        this.updateJob = function(){
            $http({
                method: 'PUT',
                url: 'dash/app',
                data: this.infoJob
            }).then(
                function(data) {
                    store.jobsList[store.index]= store.infoJob;
                    store.error = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                }
            );
        };

        this.updateUser = function(user){
            $http({
                method: 'PUT',
                url: 'dash/user',
                data: user
            }).then(
                function(data) {
                    var index = store.usersList.indexOf(user);
                    if (index > -1){
                        store.usersList[store.index]= user;
                    }
                    store.error = null;
                },
                function(error) {
                    store.error = 'Error: '+error.data.error;
                }
            );
        };

        this.infoStorage = function(storage) {
            var index = this.storagesList.indexOf(storage);
            store.infoStorage = store.storagesList[index];
            store.index = index;
        };

        this.infoAppFun = function(app) {
            var index = this.appsList.indexOf(app);
            store.infoApp = store.appsList[index];
            store.index = index;
        };

        this.editJobFun = function(job) {
            var index = this.jobsList.indexOf(job);
            store.infoJob = store.jobsList[index];
            store.index = index;
        };

        this.infoJobFun = function(job) {
            var index = this.jobsList.indexOf(job);
            store.infoJob = store.jobsList[index];
            store.index = index;
        };

        this.removeArg = function (index) {
            var idx = this.newApp.args.indexOf(index);
            if (idx > -1){
                this.newApp.args.splice(idx, 1);
            }
        };

        this.addArg = function (arg) {
            var newArg = {name:arg.name, defaultV:arg.defaultV, prefix:arg.prefix, file:arg.file, optional:arg.optional}
            this.newApp.args.push(newArg);
            store.arg = null;
        };

        this.collectChecked = function(job) {
            if (job.isChecked){
                //var newJobId = job._id;
                var newJobId = job.pmesID;
                this.selectedJobs.push(newJobId);
            } else {
                //var idx = this.selectedJobs.indexOf(job._id);
                var idx = this.selectedJobs.indexOf(job.pmesID);
                if (idx > -1){
                    this.selectedJobs.splice(idx,1);
                }
            }

        };

        this.getUser = function(){
            $http({
                method: 'GET',
                url: 'dash/user'
            }).then(
                function(data) {
                    store.user = data.data[0];
                    store.userS._id = data.data[0]._id;
                    store.error = null;
                },
                function(error) {
                    console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                    store.error = 'Error: '+error.data.error;
                }
            );
        };

        this.getImagesList = function(){
            $http({
                method: 'POST',
                url: 'dash/images',
                data: this.user
            }).then(
                function(data) {
                    store.imagesList = data.data;
                    store.error = null;
                },
                function(error) {
                    console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                    store.error = 'Error: '+error.data.error;
                }
            );
        };
        this.logout = function(){
            $http.get('auth/logout')
                .then(
                    function(data) {
                        $state.go('login', {}, { reload: true});
                    },
                    function(error) {
                        console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                        store.error = 'Error: '+error.data.error;
                    });
        };

        this.getJobsList = function() {
            $http.get('dash/jobs')
                .then(
                    function(data) {
                        store.jobsList = data.data;
                    },
                    function(error) {
                        console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                        store.error = 'Error: '+error.data.error;
                    });
        };

        this.getAppsList = function() {
            $http.get('dash/apps')
                .then(
                    function(data) {
                        store.appsList = data.data;
                    },
                    function(error) {
                        console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                        store.error = 'Error: '+error.data.error;
                    });
        };

        this.getStorageList = function() {
            $http.get('dash/storages')
                .then(
                  function(data) {
                      store.storagesList = data.data;
                  }, function(error){
                        console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                        store.error = 'Error: '+error.data.error;
                    }
                );
        };

        this.getUsersList = function() {
            $http.get('dash/users')
                .then(
                    function(data) {
                        store.usersList = data.data;
                    }, function(error){
                        console.log('OH NO, SOOMETHING HAS FAILED! AND NOBODY CARES');
                        store.error = 'Error: '+error.data.error;
                    }
                );
        };

        //Init values
        this.getJobsList();
        this.getAppsList();
        this.getStorageList();
        this.getImagesList();
        this.getUsersList();
        this.getUser();
    }]);