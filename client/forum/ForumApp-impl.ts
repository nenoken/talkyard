/**
 * Copyright (C) 2014 Kaj Magnus Lindberg (born 1979)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/// <reference path="typedefs/angularjs/angular.d.ts" />
/// <reference path="ForumApp.ts" />
/// <reference path="list-topics/ListTopicsController.ts" />
/// <reference path="list-categories/ListCategoriesController.ts" />

//------------------------------------------------------------------------------
   module forum {
//------------------------------------------------------------------------------


forum.forumApp.config(['$stateProvider', '$urlRouterProvider', configForumApp]);
forum.forumApp.run(['$rootScope', '$state', '$stateParams', runForumApp]);


function configForumApp($stateProvider, $urlRouterProvider) {
  $urlRouterProvider.otherwise('/');

  $stateProvider
    .state('index', {
      url: '/',
      template: 'index template',
      controller: function() {
        console.log('index controller');
      }
    })
    .state('latest', {
      url: '/latest/*categoryPath',
      templateUrl: 'list-topics/list-topics.html',
      controller: 'ListTopicsController'
    })
    .state('top', {
      url: '/top/*categoryPath',
      templateUrl: 'list-topics/list-topics.html',
      controller: 'ListTopicsController'
    })
    .state('categories', {
      url: '/categories',
      templateUrl: 'list-categories/list-categories.html',
      controller: 'ListCategoriesController'
    })
};


/**
 * Adds UI-Router's $state and $stateParams to the root scope, so they're
 * accessible from everywhere.
 */
function runForumApp($rootScope, $state, $stateParams) {
  $rootScope.$state = $state;
  $rootScope.$stateParams = $stateParams;
};


//------------------------------------------------------------------------------
   }
//------------------------------------------------------------------------------
// vim: fdm=marker et ts=2 sw=2 tw=0 fo=tcqwn list
