/* eslint no-alert: 0 */

'use strict';



function toQueryStirng(obj) {
  var str = [];
  for(var p in obj)
    if (obj.hasOwnProperty(p)) {
      str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
    }
  return str.join("&");
}

function removeDuplicatesByField(thearray, field) {
	var seen = {};
    thearray.filter(function(item) {
		var fieldvalue = item[field];
		if (!seen.hasOwnProperty(fieldvalue)) {
			seen[fieldvalue] = item;
		}
    });
	var ret = [];
	for (var key in seen) {
		var item = seen[key];
		ret.push(item);
	}

	return ret;
}

//
// Here is how to define your module
// has dependent on mobile-angular-ui
//
var app = angular.module('MobileAngularUiExamples', [
  'ngRoute',
  'mobile-angular-ui',

  // touch/drag feature: this is from 'mobile-angular-ui.gestures.js'.
  // This is intended to provide a flexible, integrated and and
  // easy to use alternative to other 3rd party libs like hammer.js, with the
  // final pourpose to integrate gestures into default ui interactions like
  // opening sidebars, turning switches on/off ..
  'mobile-angular-ui.gestures',
  
  'integralui',
]);

app.run(function($transform) {
  window.$transform = $transform;
});

//
// You can configure ngRoute as always, but to take advantage of SharedState location
// feature (i.e. close sidebar on backbutton) you should setup 'reloadOnSearch: false'
// in order to avoid unwanted routing.
//
app.config(function($routeProvider) {
  $routeProvider.when('/', {templateUrl: 'home.html', reloadOnSearch: false});
  $routeProvider.when('/scroll', {templateUrl: 'scroll.html', reloadOnSearch: false});
  $routeProvider.when('/toggle', {templateUrl: 'toggle.html', reloadOnSearch: false});
  $routeProvider.when('/tabs', {templateUrl: 'tabs.html', reloadOnSearch: false});
  $routeProvider.when('/accordion', {templateUrl: 'accordion.html', reloadOnSearch: false});
  $routeProvider.when('/overlay', {templateUrl: 'overlay.html', reloadOnSearch: false});
  $routeProvider.when('/forms', {templateUrl: 'forms.html', reloadOnSearch: false});
  $routeProvider.when('/dropdown', {templateUrl: 'dropdown.html', reloadOnSearch: false});
  $routeProvider.when('/touch', {templateUrl: 'touch.html', reloadOnSearch: false});
  $routeProvider.when('/swipe', {templateUrl: 'swipe.html', reloadOnSearch: false});
  $routeProvider.when('/drag', {templateUrl: 'drag.html', reloadOnSearch: false});
  $routeProvider.when('/drag2', {templateUrl: 'drag2.html', reloadOnSearch: false});
  $routeProvider.when('/carousel', {templateUrl: 'carousel.html', reloadOnSearch: false});
  
  
  $routeProvider.when('/sent', {templateUrl: 'sent.html', reloadOnSearch: false});
  $routeProvider.when('/received', {templateUrl: 'received.html', reloadOnSearch: false});
  
  $routeProvider.when('/prediction-detail/:prediction_id', {templateUrl: 'prediction-detail.html', reloadOnSearch: false});
  $routeProvider.when('/new-prediction/:receiver_username', {templateUrl: 'new-prediction.html', reloadOnSearch: false});
  $routeProvider.when('/new-prediction/', {templateUrl: 'new-prediction.html', reloadOnSearch: false});
  
  $routeProvider.when('/cms-root', {templateUrl: 'cms-root.html', reloadOnSearch: false});
  $routeProvider.when('/cms-content/:item_id', {templateUrl: 'cms-content.html', reloadOnSearch: false});
  

  
  // $routeProvider.when('/cms-tree', {templateUrl: 'cms-tree.html', reloadOnSearch: false, controller: "CMSTreeController"});
  
  
});

//
// `$touch example`
//

app.directive('toucharea', ['$touch', function($touch) {
  // Runs during compile
  return {
    restrict: 'C',
    link: function($scope, elem) {
      $scope.touch = null;
      $touch.bind(elem, {
        start: function(touch) {
          $scope.containerRect = elem[0].getBoundingClientRect();
          $scope.touch = touch;
          $scope.$apply();
        },

        cancel: function(touch) {
          $scope.touch = touch;
          $scope.$apply();
        },

        move: function(touch) {
          $scope.touch = touch;
          $scope.$apply();
        },

        end: function(touch) {
          $scope.touch = touch;
          $scope.$apply();
        }
      });
    }
  };
}]);

//
// `$drag` example: drag to dismiss
//
app.directive('dragToDismiss', function($drag, $parse, $timeout, $location) {
  return {
    restrict: 'A',
    compile: function(elem, attrs) {
      var dismissFn = $parse(attrs.dragToDismiss);
      return function(scope, elem) {
        var dismiss = false;

        $drag.bind(elem, {
          transform: $drag.TRANSLATE_RIGHT,
          move: function(drag) {
            if (drag.distanceX >= drag.rect.width / 4) {
              dismiss = true;
              elem.addClass('dismiss');
            } else {
              dismiss = false;
              elem.removeClass('dismiss');
            }
          },
          cancel: function() {
            elem.removeClass('dismiss');
          },
          end: function(drag) {
            if (dismiss) {
              elem.addClass('dismitted');
              $timeout(function() {
                scope.$apply(function() {
                  dismissFn(scope);
                });
              }, 300);
            } else {
              drag.reset();
            }
          }
        });
      };
    }
  };
});

//
// Another `$drag` usage example: this is how you could create
// a touch enabled "deck of cards" carousel. See `carousel.html` for markup.
//
app.directive('carousel', function() {
  return {
    restrict: 'C',
    scope: {},
    controller: function() {
      this.itemCount = 0;
      this.activeItem = null;

      this.addItem = function() {
        var newId = this.itemCount++;
        this.activeItem = this.itemCount === 1 ? newId : this.activeItem;
        return newId;
      };

      this.next = function() {
        this.activeItem = this.activeItem || 0;
        this.activeItem = this.activeItem === this.itemCount - 1 ? 0 : this.activeItem + 1;
      };

      this.prev = function() {
        this.activeItem = this.activeItem || 0;
        this.activeItem = this.activeItem === 0 ? this.itemCount - 1 : this.activeItem - 1;
      };
    }
  };
});

app.directive('carouselItem', function($drag) {
  return {
    restrict: 'C',
    require: '^carousel',
    scope: {},
    transclude: true,
    template: '<div class="item"><div ng-transclude></div></div>',
    link: function(scope, elem, attrs, carousel) {
      scope.carousel = carousel;
      var id = carousel.addItem();

      var zIndex = function() {
        var res = 0;
        if (id === carousel.activeItem) {
          res = 2000;
        } else if (carousel.activeItem < id) {
          res = 2000 - (id - carousel.activeItem);
        } else {
          res = 2000 - (carousel.itemCount - 1 - carousel.activeItem + id);
        }
        return res;
      };

      scope.$watch(function() {
        return carousel.activeItem;
      }, function() {
        elem[0].style.zIndex = zIndex();
      });

      $drag.bind(elem, {
        //
        // This is an example of custom transform function
        //
        transform: function(element, transform, touch) {
          //
          // use translate both as basis for the new transform:
          //
          var t = $drag.TRANSLATE_BOTH(element, transform, touch);

          //
          // Add rotation:
          //
          var Dx = touch.distanceX;
          var t0 = touch.startTransform;
          var sign = Dx < 0 ? -1 : 1;
          var angle = sign * Math.min((Math.abs(Dx) / 700) * 30, 30);

          t.rotateZ = angle + (Math.round(t0.rotateZ));

          return t;
        },
        move: function(drag) {
          if (Math.abs(drag.distanceX) >= drag.rect.width / 4) {
            elem.addClass('dismiss');
          } else {
            elem.removeClass('dismiss');
          }
        },
        cancel: function() {
          elem.removeClass('dismiss');
        },
        end: function(drag) {
          elem.removeClass('dismiss');
          if (Math.abs(drag.distanceX) >= drag.rect.width / 4) {
            scope.$apply(function() {
              carousel.next();
            });
          }
          drag.reset();
        }
      });
    }
  };
});

app.directive('dragMe', ['$drag', function($drag) {
  return {
    controller: function($scope, $element) {
      $drag.bind($element,
        {
          //
          // Here you can see how to limit movement
          // to an element
          //
          transform: $drag.TRANSLATE_INSIDE($element.parent()),
          end: function(drag) {
            // go back to initial position
            drag.reset();
          }
        },
        { // release touch when movement is outside bounduaries
          sensitiveArea: $element.parent()
        }
      );
    }
  };
}]);



function getAPIUrlPrefix() {
	// return "http://10.2.9.98:8000/"
	
	var loc = window.location.href;
	
	if (loc.startsWith("http://localhost")) {
		return "http://localhost:8887/";
	}
	
	return window.location.origin + "/";
}
//
// For this trivial demo we have just a unique MainController
// for everything
//

// .controller("appCtrl", ["$scope", "IntegralUITreeViewService", function($scope, $treeService){
	
	
	
// app.controller('MainController', function($rootScope, $scope, $http, $location, $routeParams) {

app.controller('MainController', function($rootScope, $scope, $http, $location, $routeParams, IntegralUITreeViewService, $timeout) {
  
  $scope.swiped = function(direction) {
    alert('Swiped ' + direction);
  };

  // User agent displayed in home page
  $scope.userAgent = navigator.userAgent;

  // Needed for the loading screen
  $rootScope.$on('$routeChangeStart', function() {
    $rootScope.loading = true;
  });

  $rootScope.$on('$routeChangeSuccess', function() {
    $rootScope.loading = false;
  });

  // Fake text i used here and there.
  $scope.lorem = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. ' +
    'Vel explicabo, aliquid eaque soluta nihil eligendi adipisci error, illum ' +
    'corrupti nam fuga omnis quod quaerat mollitia expedita impedit dolores ipsam. Obcaecati.';

  //
  // 'Scroll' screen
  //
  var scrollItems = [];

  for (var i = 1; i <= 100; i++) {
    scrollItems.push('Item ' + i);
  }

  $scope.scrollItems = scrollItems;

  $scope.bottomReached = function() {
    alert('Congrats you scrolled to the end of the list!');
  };

  //
  // Right Sidebar
  //
  $scope.chatUsers = [
    {name: 'Carlos  Flowers', online: true},
    {name: 'Byron Taylor', online: true},
    {name: 'Jana  Terry', online: true},
    {name: 'Darryl  Stone', online: true},
    {name: 'Fannie  Carlson', online: true},
    {name: 'Holly Nguyen', online: true},
    {name: 'Bill  Chavez', online: true},
    {name: 'Veronica  Maxwell', online: true},
    {name: 'Jessica Webster', online: true},
    {name: 'Jackie  Barton', online: true},
    {name: 'Crystal Drake', online: false},
    {name: 'Milton  Dean', online: false},
    {name: 'Joann Johnston', online: false},
    {name: 'Cora  Vaughn', online: false},
    {name: 'Nina  Briggs', online: false},
    {name: 'Casey Turner', online: false},
    {name: 'Jimmie  Wilson', online: false},
    {name: 'Nathaniel Steele', online: false},
    {name: 'Aubrey  Cole', online: false},
    {name: 'Donnie  Summers', online: false},
    {name: 'Kate  Myers', online: false},
    {name: 'Priscilla Hawkins', online: false},
    {name: 'Joe Barker', online: false},
    {name: 'Lee Norman', online: false},
    {name: 'Ebony Rice', online: false}
  ];
$scope.chatUsers = [];
  //
  // 'Forms' screen
  //
  
  
  
  $scope.loginForm = {
		email: '',
		password:"",
  };
  $scope.rememberMe = true;
  
  $scope.needShowLoginDialog = true;
  
  
  $scope.newPredictionForm = {
	  receiverUsername : "",
	  content : "",
  };
  
  
  
  $scope.getLocalUsername = function(){
	var username = localStorage.getItem("username");
	return username;
  }
  
  $scope.username = $scope.getLocalUsername();
  $scope.getLocalToken = function(){
	var token = localStorage.getItem("token");
	return token;
  }
  
  $scope.getLocalSessionid = function(){
	var token = localStorage.getItem("sessid");
	return token;
  }
  $scope.getLocalUid = function(){
	var token = localStorage.getItem("uid");
	return token;
  }
  
  $scope.gotoSentList = function(){
	  $location.path("/sent");
  }
  
  $scope.gotoReceivedList = function(){
	  $location.path("/received");
  }
  
  
  $scope.gotoRegister = function(){
	  console.log("$scope.gotoRegister");
	  $location.path("/register");
  }
  
  $scope.checkLocalToken = function(){
	var token = $scope.getLocalToken();
	console.log("token in localStorage: " + token);
	if (token)  {
		$scope.gotoReceivedList();
	}
  }
  $scope.logedin = true;
  $scope.checkLogedin = function() {
	  var ret = localStorage.getItem("logedin");
	  console.log("$scope.checkLogedin: ret= " + ret);
	  if (ret == null) {
		  $scope.logedin = false;
		  return false;
	  }
	  ret = (ret == 'true');
	  console.log("$scope.logedin = " + ret);
	  $scope.logedin = ret;
	  $scope.sessid = $scope.getLocalSessionid();
	  // $scope.logedin = true;
	  return ret;
  }
  
  $scope.clearLocalData = function() {
	  console.log("$scope.clearLocalData");
	  localStorage.clear();
	  return;
  }
  
  $scope.logout = function() {
	  console.log("$scope.logout");
	  $scope.clearLocalData();
	  localStorage.setItem("logedin", false);
	  $scope.logedin = false;
  }
  
  $scope.saveToLocalStorage = function() {
	  
	localStorage.setItem("logedin", $scope.logedin);
	localStorage.setItem("username", $scope.username);
	localStorage.setItem("sessid", $scope.sessid);
	localStorage.setItem("username", $scope.username);
			
	
  }
  $scope.login = function() {
	  
	  console.log("$scope.loginForm.email: " + $scope.loginForm.email);
	  console.log("$scope.loginForm.password: " + $scope.loginForm.password);
	  
	  
	  var email = $scope.loginForm.email;
	  var password = $scope.loginForm.password;
	  console.log("email: " + email);
	  console.log("password: " + password);
	  
	  
    // alert('You submitted the login form');
	var url = getAPIUrlPrefix() + "login_with_password";
	var data = toQueryStirng({
		"ident": email,
		"password": password
	});
	var req = {
	  url: url,
	  method: 'POST',
	  headers: {
	   'Content-Type': "application/x-www-form-urlencoded"
	 },
	  data: data,
	};
	
	
	$http(req).then(function successCallback(response) {
		console.log("successCallback");
		console.log(response);
		var sessid = response.data.sessid;
		var uid = response.data.uid
		console.log("sessid=" + sessid);
		console.log("uid=" + uid);
		
		localStorage.setItem("sessid", sessid);
		localStorage.setItem("uid", uid);
		
		
		if (response.data.code==200) {
			
			$scope.logedin = true;
			$scope.username = response.data.data.username;
			$scope.sessid = response.data.sessid;
			$scope.uid = response.data.uid;
			
		}
		
		$scope.uid = uid;
		// $scope.gotoSentList();
		
	  }, function errorCallback(response) {
		console.log("errorCallback");
		alert(response.data.msg);
	  });
	
	
  };
	
	
  //
  // 'Drag' screen
  //
  $scope.notices = [];

  for (var j = 0; j < 10; j++) {
    $scope.notices.push({icon: 'envelope', message: 'Notice ' + (j + 1)});
  }

  $scope.deleteNotice = function(notice) {
    var index = $scope.notices.indexOf(notice);
    if (index > -1) {
      $scope.notices.splice(index, 1);
    }
  };
  
	
	$scope.sentByMeList = [];
	$scope.sentToMeList = [];
	$scope.fetchSentByMeList = function() {
		var token = $scope.getLocalToken();
		var url = getAPIUrlPrefix() + "prediction/sent_by_me_list/?";
		url += toQueryStirng({"token": token});
		var req = {
			url: url,
			method: 'GET',
		};
	
	
		$http(req).then(function successCallback(response) {
			console.log("fetchSentByMeList successCallback");
			$scope.sentByMeList = response.data.data;
			
			var arrayLength = $scope.sentByMeList.length;
			for (var i = 0; i < arrayLength; i++) {
				var receiver = $scope.sentByMeList[i].receiver;
				if (receiver != $scope.username) {
					$scope.chatUsers.push({"name": receiver});	
				}
			}
			$scope.chatUsers = removeDuplicatesByField($scope.chatUsers, "name");
			
			console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		  });
	}
	
	
	
	$scope.fetchCMSRoot = function() {
		console.log("$scope.fetchCMSRoot");
		var url = getAPIUrlPrefix() + "get_cms_root/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		url += toQueryStirng({
				"sessid": sessid,
				"fields": "name,create_time",
				"start": "0",
				"limit":  "1000"
				});
		var req = {
			url: url,
			method: 'GET',
		};
		$http(req).then(function successCallback(response) {
			console.log("fetchCMSRoot successCallback");
			$scope.cmsRoot = response.data.data;
			
			
			console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		  });
		
		// http://localhost:8887/get_cms_root?sessid=test-sess-26&fields=name,create_time&start=0&limit=20
	}
	
	
	
	$scope.fetchCMSItem = function(item_id) {
		console.log("$scope.fetchCMSItem");
		var url = getAPIUrlPrefix() + "get_cms_item/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		url += toQueryStirng({
				"sessid": sessid,
				"fields": "name,title,create_time,content", // 这里还可以加其他需要的字段。
				"id": item_id,
				});
		var req = {
			url: url,
			method: 'GET',
		};
		$http(req).then(function successCallback(response) {
			console.log("fetchCMSRoot successCallback");
			$scope.currentCMSItem = response.data.data;
			
			
			console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		  });
		
		// http://localhost:8887/get_cms_root?sessid=test-sess-26&fields=name,create_time&start=0&limit=20
		
		
	}
	
	
	
	$scope.fetchSentToMeList = function() {
		var token = $scope.getLocalToken();
		var url = getAPIUrlPrefix() + "prediction/sent_to_me_list/?";
		url += toQueryStirng({"token": token});
		var req = {
			url: url,
			method: 'GET',
		};
	
	
		$http(req).then(function successCallback(response) {
			console.log("sentToMeList successCallback");
			$scope.sentToMeList = response.data.data;
			var arrayLength = $scope.sentByMeList.length;
			for (var i = 0; i < arrayLength; i++) {
				var sender = $scope.sentByMeList[i].sender;
				if (sender != $scope.username) {
					$scope.chatUsers.push(sender);	
				}
			}
			$scope.chatUsers = removeDuplicatesByField($scope.chatUsers, "name");
			console.log(response);
		  }, function errorCallback(response) {
			console.log("sentToMeList errorCallback");
		  });
	}
	$scope.$on('$locationChangeStart', function(event) {
		console.log('$locationChangeStart');
	});
   
   
    $scope.findPredictionById = function(id) {
		
		var arrayLength = $scope.sentByMeList.length;
		for (var i = 0; i < arrayLength; i++) {
			if ($scope.sentByMeList[i].id == id) {
				return $scope.sentByMeList[i];
			}
		}
		for (var i = 0; i < arrayLength; i++) {
			if ($scope.sentToMeList[i].id == id) {
				return $scope.sentToMeList[i];
			}
		}
		return null;		
	};
	
	$scope.currentPrediction = null;
	$scope.$on('$routeChangeSuccess', function (next, last) {
		$scope.sessid = $scope.getLocalSessionid();
		
		
		console.log('$routeChangeSuccess: ' + $location.$$path);
		if ($location.$$path == "/sent") {
			$scope.fetchSentByMeList();
		} else if ($location.$$path == "/received") {
			$scope.fetchSentToMeList();
		} else if ($location.$$path.startsWith("/new-prediction/")) {
			
			
			
			if ($routeParams.receiver_username == undefined) {
				
				$scope.newPredictionForm.receiverUsername = "";
			} else {
				
				$scope.newPredictionForm.receiverUsername = $routeParams.receiver_username;	
			}
			
			console.log("$scope.newPredictionForm.receiverUsername: " + $scope.newPredictionForm.receiverUsername);
			
		} else if ($location.$$path.startsWith("/prediction-detail/")) {
			var prediction_id = $routeParams.prediction_id
			console.log($routeParams);
			console.log("prediction_id: " + prediction_id);
			
			

			$scope.currentPrediction = $scope.findPredictionById(prediction_id);
			
			
			console.log("$scope.currentPrediction: ");
			console.log($scope.currentPrediction);
			
			
			console.log("$scope.username: " + $scope.username);
		} else if ($location.$$path.startsWith("/cms-root")) {
			
			$scope.fetchCMSRoot();
		} else if ($location.$$path.startsWith("/cms-content")) {
			var item_id = $routeParams.item_id;
			console.log("/cms-content, item_id: " + item_id);
			$scope.fetchCMSItem(item_id);
		} else if ($location.$$path.startsWith("/")) {
			console.log("changed to / .");
			$scope.checkLogedin();
		} 
		
	});
	$scope.new_prediction = function() {
		var content = $scope.newPredictionForm.content;
		var receiver_username = $scope.newPredictionForm.receiverUsername;
		
		
		console.log("$scope.new_prediction");
		
		
		
		var url = getAPIUrlPrefix() + "prediction/create_new/";
		var data = toQueryStirng({
				"token": $scope.getLocalToken(),
				"content": content, 
				"receiver_username": receiver_username, 
				
				});
		var req = {
		  url: url,
		  method: 'POST',
		  headers: {
		   'Content-Type': "application/x-www-form-urlencoded"
		 },
		  data: data
		};
		
		
		$http(req).then(function successCallback(response) {
			console.log("new_prediction.successCallback");
			console.log(response);
			if (response.data.errcode == 200) {
				alert("发送成功。");
				// $scope.content = "";
				// $scope.receiver_username = "";
				
			}
			
		  }, function errorCallback(response) {
			console.log("new_prediction.errorCallback");
		  });
		  
		  
	  
	  
	}
	// $scope.checkLocalToken();
	$scope.checkLogedin();
	
	
	
	
	
	
	
	
	
	
	
});

app.controller('CMSTreeControllerLearn', function($rootScope, $scope, $http, $location, $routeParams, IntegralUITreeViewService, $timeout) {
	console.log("CMSTreeController");
	
	
	
	// treeview的调研:
	// var $treeService = IntegralUITreeViewService;
	$scope.cmsTreeData = [];
		$scope.treeName = 'sampleTree';

		var sampleData = [];
		
		var timer = $timeout(function(){
			// Read data from a JSON file using $http methods
			var dataSource = $http.get('sample-data.json');
			if (dataSource){
				dataSource.success(function(data){
					sampleData = data;
					
					// At first only populate the TreeView with root items
					extractData();
				});
				dataSource.error(function(data){
					alert("AJAX failed to Load Data");
				});
			}
			
			$timeout.cancel(timer);
		}, 100);
		
		var isThereChildItems = function(parentId){
			for (var i = 0; i < sampleData.length; i++){
				if (sampleData[i].pid === parentId)
					return true;
			}
			
			//return false;
			return true;
		}
		
		var getItem = function(index){
			var item = {
				id: sampleData[index].id,
				pid: sampleData[index].pid,
				text: sampleData[index].text,
				expanded: sampleData[index].expanded,
				hasChildren: sampleData[index].hasChildren
			}
			
			// In order to show the expand box, create a
			// temporary item which will act as a child item
			
			//if (isThereChildItems(sampleData[index].id))
			//	item.items = [{ text: "tmp" }];
				
			return item;
		}
		
		var extractData = function(){
			// Extract only root items
			for (var i = 0; i < sampleData.length; i++){
				if (sampleData[i].pid === undefined)
					$scope.cmsTreeData.push(getItem(i));
			}

			IntegralUITreeViewService.updateLayout($scope.treeName);
		}
		
		// Whenever item is expanding, add child items to the expanding item
		var loadOnDemand = function(item){
			var childItems = [];
			for (var i = 0; i < sampleData.length; i++){
				if (sampleData[i].pid === item.id)
					childItems.push(getItem(i));
			}
			
			item.items = childItems;
		}
		
		// Before item is expanded, call the loadOnDemand method
		// which will populate the item with its child items
		$scope.onBeforeExpandCMSTree = function(e){
			if (e.item.items && e.item.items.length > 1)
				return;
			else {
				IntegralUITreeViewService.beginLoad($scope.treeName, e.item);
				
				var loadTImer = $timeout(function(){
					loadOnDemand(e.item);
					
					IntegralUITreeViewService.updateLayout($scope.treeName);
						
					IntegralUITreeViewService.endLoad($scope.treeName, e.item);
				}, 1000);

				//if (e.item.items)
				//	e.item.items.splice(0, 1);
			}
		}
});


app.controller('CMSTreeController', function($rootScope, $scope, $http, $location, $routeParams, IntegralUITreeViewService, $timeout) {
	console.log("CMSTreeController");
	$scope.cmsTreeData = [];
	$scope.treeName = 'sampleTree';
	$scope.fetchCMSRoot = function() {
		console.log("$scope.fetchCMSRoot in CMSTreeController");
		var url = getAPIUrlPrefix() + "get_cms_root/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		// http://localhost:8887/get_cms_root?sessid=test-sess-26&fields=name,create_time&start=0&limit=20
		url += toQueryStirng({
				"sessid": sessid,
				"fields": "name,create_time,title",
				"start": "0",
				"limit":  "1000"
				});
		var req = {
			url: url,
			method: 'GET',
		};
		$http(req).then(function successCallback(response) {
			console.log("fetchCMSRoot successCallback");
			$scope.cmsRoot = response.data.data;
			for (var i = 0; i < response.data.data.length; i++){
				var item = response.data.data[i];
				item["id"] = item["item_id"];
				item["text"] = item["global_name"];
				item["expanded"] = false;
				item["hasChildren"] = true;
				item["isRoot"] = true;
				$scope.cmsTreeData.push(item);
			}
			// console.log($scope.cmsTreeData);
			IntegralUITreeViewService.updateLayout($scope.treeName);
			console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		  });
	}
	
	$scope.fetchCMSMembers = function(item) {
		var id = item["id"];
		console.log("$scope.fetchCMSMembers, id: " + id);
		
		
		
		
		
		var url = getAPIUrlPrefix() + "get_cms_members/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		// http://localhost:8887/get_cms_members?sessid=test-sess-26&fields=name,create_time&start=0&limit=20&container_id=XXX
		url += toQueryStirng({
				"sessid": sessid,
				"fields": "name,create_time,title",
				"container_id": id,
				"start": "0",
				"limit":  "1000"
				});
		var req = {
			url: url,
			method: 'GET',
		};
		$http(req).then(function successCallback(response) {
			console.log("fetchCMSMembers successCallback");
			
			var childItems = [];
			
			for (var i = 0; i < response.data.data.length; i++){
				var itm = response.data.data[i];
				
				itm["text"] = itm["id"];
				if (itm["name"] != null)
					itm["text"] = itm["name"];
				
				if (itm["title"] != null)
					itm["text"] = itm["title"];
				
				itm["expanded"] = false;
				itm["hasChildren"] = true;
				itm["isRoot"] = false;
				itm["pid"] = id;
				// $scope.cmsTreeData.push(item);
				
				childItems.push(itm);
				
			}
			console.log("childItems: ");
			console.log(childItems);
			item.items = childItems;
			IntegralUITreeViewService.updateLayout($scope.treeName);
			
			
			console.log("endLoad: ");
			IntegralUITreeViewService.endLoad($scope.treeName, item);
			// console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		});
		  
		  
		
		
	}
	$scope.afterSelectCMSTree = function(e) {
		console.log("$scope.afterSelectCMSTree: " + e.item["id"] + " isRoot: " + e.item["isRoot"] );
	}
	$scope.onBeforeExpandCMSTree = function(e){
		console.log("$scope.onBeforeExpandCMSTree: " + e.item["id"] + " isRoot: " + e.item["isRoot"] );
		
		
		
		if (e.item.items && e.item.items.length > 1) {
			console.log("e.item.items && e.item.items.length > 1");
			return;
		}
		else {
			IntegralUITreeViewService.beginLoad($scope.treeName, e.item);
			$scope.fetchCMSMembers(e.item);
			
			// var loadTImer = $timeout(function(){
			// 	// loadOnDemand(e.item);
			// 	IntegralUITreeViewService.updateLayout($scope.treeName);
			// 	IntegralUITreeViewService.endLoad($scope.treeName, e.item);
			// }, 1000);
            // 
			// if (e.item.items)
			// 	e.item.items.splice(0, 1);
		}
	}
		
	$scope.unsetGlobalName = function(){
		console.log("$scope.unsetGlobalName");
		
		var selectedItem = IntegralUITreeViewService.selectedItem($scope.treeName);
		
		console.log(selectedItem["id"]);
	}
	$scope.inputGlobalName = "";
	$scope.setGlobalName = function(){
		console.log("$scope.setGlobalName: " + $scope.inputGlobalName);
		if ($scope.inputGlobalName == null || $scope.inputGlobalName == "") {
			console.log("Global name must not be empty.");
			return;
		}
		var selectedItem = IntegralUITreeViewService.selectedItem($scope.treeName);
		console.log(selectedItem["id"]);
		
		
		var url = getAPIUrlPrefix() + "set_cms_global_name/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		// http://localhost:8887/set_cms_global_name
		url += toQueryStirng({
				"sessid": sessid,
				"item_id": selectedItem["id"],
				"replace_on_exist": "0",
				"global_name": $scope.inputGlobalName,
				});
		var req = {
			url: url,
			method: 'POST',
		};
		$http(req).then(function successCallback(response) {
			console.log("setGlobalName successCallback");
			alert("设置全局名成功");
			selectedItem["text"] = $scope.inputGlobalName;
			IntegralUITreeViewService.addItem($scope.treeName, selectedItem);
			// IntegralUITreeViewService.updateLayout($scope.treeName);
		  }, function errorCallback(response) {
			console.log("setGlobalName errorCallback");
			if (response.status==400) {
				if (response.data.code == 400) {
					alert("设置全局名失败: " + response.data.msg);
				}
				
			}
			
			
			
		});
	}
	
	
	
	$scope.shouldDisableUnsetGlobalName = function(){
		
		var selectedItem = IntegralUITreeViewService.selectedItem($scope.treeName);
		if (selectedItem == null)
			return true;
		if (selectedItem["isRoot"] == true)
			return false;
		return true;
	}
	
	
	$scope.shouldDisableSetGlobalName = function(){
		
		
		var selectedItem = IntegralUITreeViewService.selectedItem($scope.treeName);
		if (selectedItem == null)
			return true;
		if (selectedItem["isRoot"] == true)
			return true;
		return false;
		
	}
		
		
	$scope.fetchCMSRoot();
});

app.config(function($routeProvider) {
  $routeProvider.when('/cms-tree', {templateUrl: 'cms-tree.html', reloadOnSearch: false, controller: "CMSTreeController"});
});


app.controller('VirtualAssetsController', function($rootScope, $scope, $http, $location, $routeParams, IntegralUITreeViewService, $timeout) {
	console.log("VirtualAssetsController");
	$scope.assetNames = "va_coder_coin,va_diamond";
	
	
	$scope.fetchMyVirtualAssets = function() {
		console.log("$scope.fetchMyVirtualAssets in VirtualAssetsController");
		
		var url = getAPIUrlPrefix() + "get_my_virtual_assets/?";
		var sessid = $scope.getLocalSessionid();
		console.log("sessid: " + sessid);
		//	http://localhost:8887/get_my_virtual_assets?sessid=sess-3y8g82de54&asset_names=va_coder_coin
		url += toQueryStirng({
				"sessid": sessid,
				"asset_names": $scope.assetNames,
				});
		var req = {
			url: url,
			method: 'GET',
		};
		$http(req).then(function successCallback(response) {
			console.log("fetchMyVirtualAssets successCallback");
			// $scope.cmsRoot = response.data.data;
			var arr = [];
			for (var va_name in response.data.data) {
				var item = {
					name: va_name,
					amount: response.data.data[va_name],
				}
				arr.push(item);
			}
			$scope.virtualAssets = arr;
			console.log(response);
		  }, function errorCallback(response) {
			console.log("fetchSentByMeList errorCallback");
		  });
	}
	$scope.incrVirtualAssets = function() {
		console.log("$scope.incrVirtualAssets in VirtualAssetsController");
		console.log("$scope.incrAssetName: " + $scope.incrAssetName);
		console.log("$scope.incrAssetAmount: " + $scope.incrAssetAmount);
		
		
		var url = getAPIUrlPrefix() + "incr_virtual_assets";
		var sessid = $scope.getLocalSessionid();
		
		var data ={
			"sessid": sessid,
			"uid": $scope.getLocalUid(),
		};
		data[$scope.incrAssetName] = $scope.incrAssetAmount;
		
		data = toQueryStirng(data);
		var req = {
			url: url,
			method: 'POST',
			headers: {
				'Content-Type': "application/x-www-form-urlencoded"
			},
			data: data,
		};
		$http(req).then(function successCallback(response) {
			console.log("incrVirtualAssets successCallback");
			$scope.fetchMyVirtualAssets();
		  }, function errorCallback(response) {
			console.log("incrVirtualAssets errorCallback");
			alert(response.data.msg);
		  });
	}
	
	
	
	
	
	$scope.incrAssetName = "va_coder_coin"
	$scope.incrAssetAmount = 0;
	
	
	$scope.fetchMyVirtualAssets();
});

app.config(function($routeProvider) {
	
    $routeProvider.when('/my-virtual-assets', {templateUrl: 'my-virtual-assets.html', reloadOnSearch: false, controller: "VirtualAssetsController"});
});

app.controller('RegisterController', function($rootScope, $scope, $http, $location, $routeParams, IntegralUITreeViewService, $timeout) {
	console.log("RegisterController");
	$scope.registerForm = {
	//	"username": "",
	//	"password": "",
	};
	
	$scope.registerForm.username = "";
	$scope.registerForm.password = "";
	$scope.registerForm.password2 = "";
	
	
	
	$scope.submitRegisterForm = function(){
		console.log("$scope.submitRegisterForm");
		if ($scope.registerForm.username == "") {
			alert("请输入用户名。");
			return;
		}
		if ($scope.registerForm.password == "") {
			alert("请输入密码。");
			return;
		}
		if ($scope.registerForm.password != $scope.registerForm.password2) {
			alert("两次输入的密码不一致。");
			return;
		} 
		
		// http://localhost:8887/register
		// 
		
		var url = getAPIUrlPrefix() + "register";
		var sessid = $scope.getLocalSessionid();
		
		var data ={
			"sessid": sessid,
			"username": $scope.registerForm.username,
			"password": $scope.registerForm.password
		};
		
		data = toQueryStirng(data);
		var req = {
			url: url,
			method: 'POST',
			headers: {
				'Content-Type': "application/x-www-form-urlencoded"
			},
			data: data,
		};
		$http(req).then(function successCallback(response) {
			console.log("register successCallback");
			var uid = response.data.data.uid;
			var username = response.data.data.username;
			var sessid = response.data.data.sessid;
			

  
			localStorage.setItem("sessid", sessid);
			localStorage.setItem("uid", uid);
			localStorage.setItem("username", username);			
			localStorage.setItem("logedin", true);
			$location.path("/");
		  }, function errorCallback(response) {
			console.log("register errorCallback");
			alert(response.data.msg);
		  });
		  
		
	};
	
});

app.config(function($routeProvider) {
	
    $routeProvider.when('/register', {templateUrl: 'register.html', reloadOnSearch: false, controller: "RegisterController"});
});












