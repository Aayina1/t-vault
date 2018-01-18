/*
* =========================================================================
* Copyright 2017 T-Mobile USA, Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* =========================================================================
*/

'use strict';

(function(app){
    app.controller('AppCtrl', function($scope, $rootScope, RefreshHandler, $state){
      $rootScope.goTo = function(state) {
        try {
          $state.go(state);
        } catch (e) {
          console.log(e);
        }
      }
      $rootScope.$on('$stateChangeSuccess', function (ev, to, toParams, from, fromParams) {
         $rootScope.lastVisited = from.name;
      });
        $rootScope.modalParent = {};
        RefreshHandler.loadFromWebstorage();
        $rootScope.$on( '$stateChangeStart', function( ev, to, toParams, from ) {
            $rootScope.modalParent.previousState = from.name;
            $rootScope.modalParent.currentState = to.name;
        })
    })
})(angular.module('pacman'));
