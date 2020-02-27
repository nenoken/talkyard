/// <reference path="../test-types.ts"/>

import * as _ from 'lodash';
import assert = require('assert');
import fs = require('fs');
import server = require('../utils/server');
import utils = require('../utils/utils');
import { buildSite } from '../utils/site-builder';
import pagesFor = require('../utils/pages-for');
import settings = require('../utils/settings');
import lad = require('../utils/log-and-die');
import c = require('../test-constants');

declare var browser: any;
declare var browserA: any;
declare var browserB: any;

let everyonesBrowsers;
let richBrowserA;
let richBrowserB;
let owen: Member;
let owensBrowser;
let maria: Member;
let mariasBrowser;
let michael: Member;
let michaelsBrowser;
let strangersBrowser;

let siteIdAddress: IdAddress;
let siteId;

let forum: TwoPagesTestForum;  // or: LargeTestForum

const localHostname = 'comments-for-e2e-test-scrlld-localhost-8080';
const embeddingOrigin = 'http://e2e-test-scrlld.localhost:8080';
const pageDddSlug = 'emb-cmts-ddd.html';

let discussionPageUrl: string;


describe("emb-cmts-scroll-load-post  TyT502RKHFBN5", () => {

  it("import a site", () => {
    const builder = buildSite();
    forum = builder.addTwoPagesForum({
      title: "Emb Cmts Scroll Load Posts E2E Test",
      members: undefined, // default = everyone
    });
    for (let i = 0; i <= 100; ++i) {
      builder.addPost({
        page: forum.topics.byMichaelCategoryA,
        nr: c.FirstReplyNr + i,
        parentNr: c.BodyNr,
        authorId: forum.members.maria.id,
        approvedSource: `Michael! I have ${i} things on my mind, where shall I start?`,
      });
    }
    assert(builder.getSite() === forum.siteData);

    const michaelsPage = _.find(
        forum.siteData.pages, p => p.id === forum.topics.byMichaelCategoryA.id);
    michaelsPage.role = c.TestPageRole.EmbeddedComments;

    forum.siteData.meta.localHostname = localHostname;
    forum.siteData.settings.allowEmbeddingFrom = embeddingOrigin;
    //forum.siteData.meta.name = 'e2e-test-' + 'scrlld'

    siteIdAddress = server.importSiteData(forum.siteData);
    siteId = siteIdAddress.id;
    server.skipRateLimits(siteId);
    discussionPageUrl = siteIdAddress.origin + '/' + forum.topics.byMichaelCategoryA.slug;
  });

  it("initialize people", () => {
    everyonesBrowsers = _.assign(browser, pagesFor(browser));
    richBrowserA = _.assign(browserA, pagesFor(browserA));
    richBrowserB = _.assign(browserB, pagesFor(browserB));

    owen = forum.members.owen;
    owensBrowser = richBrowserA;

    maria = forum.members.maria;
    mariasBrowser = richBrowserB;
    michael = forum.members.michael;
    michaelsBrowser = richBrowserB;
    strangersBrowser = richBrowserB;
  });

  const pageSlug = 'load-and-scroll.html';

  it("There's an embedding page", () => {
    const dir = 'target';
    fs.writeFileSync(`${dir}/${pageSlug}`, makeHtml('b3c-aaa', '#444'));
    function makeHtml(pageName: string, bgColor: string): string {
      return utils.makeEmbeddedCommentsHtml({
        pageName,
        talkyardPageId: forum.topics.byMichaelCategoryA.id,
        localHostname,
        bgColor});
    }
  });

  it("A stranger wants to read #comment-30, which needs to be lazy-opened", () => {
    strangersBrowser.go(embeddingOrigin + '/' + pageSlug);
  });


});

