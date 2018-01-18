/*
* =========================================================================
* Copyright 2018 T-Mobile, US
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
* See the readme.txt file for additional language around disclaimer of warranties.
* =========================================================================
*/

'use strict';
(function(app) {
    app.directive('elementProperties', ["$rootScope", "$document", function($rootScope, $document) {
        return {
            restrict: 'A',
            scope: {
                elementHeight: '=',
            },
            link: function(scope, element, attrs) {
                var offsetHeight = element[0].offsetHeight;
                var periBodyHeight = offsetHeight - 153;
                var valueOfPeriHeight = periBodyHeight + "px";
                scope.elementHeight = valueOfPeriHeight; 
            }
        }
    }]);
})(angular.module('pacman.features.ElementProperties', []));
