/*
 * Copyright (c) 2015-2019 Kaj Magnus Lindberg
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

type PageId = string;
type PostId = number;
type PostNr = number;
type DraftNr = number;
type PageVersion = number;
type CategoryId = number;
type SiteId = number;
type SiteVersion = number;
type LoginId = String;
type UserId = number;
type PeopleId = UserId;
type PermissionId = number;
type NotificationId = number;
type ReviewTaskId = number;
type IdentityId = String;
type IpAddress = String;
type EmailId = String;
type AuditLogEntryId = number;
type TagLabel = string;
type ApiSecretNr = number;
type WhenMs = number;
type ExtId = string;
type ExtImpId = ExtId; // RENAME to ExtId

type U = undefined;


const enum ReviewDecision {
  // 1nnn = Accept.
  Accept = 1001,
  // 3nnn = Request changes.
  // 5nnn = Reject.
  DeletePostOrPage = 5001,
}


const enum FlagType {
  Spam = 51,
  Inapt = 52,
  Other = 53,
}


const enum DeletedStatus {
  SelfBit = 1,
  SuccessorsBit = 2,
  TreeBits = SelfBit | SuccessorsBit,
  AncestorsBit = 4,
  AllBits = SelfBit | SuccessorsBit | AncestorsBit,
}


const enum DraftStatus {  // sync with test code [5ABXG20]
  NotLoaded = 0,
  NothingHappened = 1,
  EditsUndone = 2,
  SavedServerSide = 3,
  SavedInBrowser = 31,
  Deleted = 4,
  NeedNotSave = Deleted,
  ShouldSave = 5,
  SavingSmall = 6,
  SavingBig = 7,
  Deleting = 8,
  CannotSave = 10,
}


// draftType_toPostType() can convert to PostType.
const enum DraftType {
  Scratch = 1,
  Topic = 2,
  DirectMessage = 3,
  Edit = 4,
  Reply = 5,
  ProgressPost = 6,
}


// postType_toDraftType() can convert to DraftType.
const enum PostType {   // sync with test code [26BKA01]
  Normal = 1,         // RENAME to NormalPost
  Flat = 2,           // CLEAN_UP remove
  ChatMessage = 3,
  BottomComment = 4,  // RENAME to ProgressPost
  StaffWiki = 11,
  CommunityWiki = 12,
  CompletedForm = 21,
  MetaMessage = 31,   // RENAME to MetaPost
}


const enum PostVoteType {
  Like = 41,
  Disagree = 42,
  Bury = 43,
  Unwanted = 44,
}


const enum PageNotfLevel {
  EveryPostAllEdits = 9,
  EveryPost = 8,
  TopicProgress = 7,
  TopicSolved = 6,
  NewTopics = 5,
  Tracking = 4,
  Normal = 3,
  Hushed = 2,
  Muted = 1,
}


const enum NotificationType {
  DirectReply = 301,
  Mention = 302,  // DirectMention
  // GroupMention =
  // Quote = 3,
  Message = 304,   // rename to DirectMessage
  NewPost = 305,
  // NewPage =      // Add
  PostTagged = 406,
}


const enum EmailNotfPrefs {
  ReceiveAlways = 5,
  Receive = 1,
  DontReceive = 2,
  ForbiddenForever = 3,
  Unspecified = 4,
}


const enum IncludeInSummaries {
  Default = 0,
  YesFeatured = 1,
  NoExclude = 3
}


// Either a TopicListLayout enum value, or a CategoriesLayout, or a TopicLayout.
type PageLayout = number;


const enum TopicListLayout {
  Default = 0,
  TitleOnly = 1,
  TitleExcerptSameLine = 2,
  ExcerptBelowTitle = 3,
  ThumbnailLeft = 4,
  ThumbnailsBelowTitle = 5,
  NewsFeed = 6,
}


const enum CategoriesLayout {
  Default = 0,
}


const enum TopicLayout {
  Default = 0,  // then, depends on topic type. E.g. question-answers —> threaded discussion.
  ThreadedDiscussion = 1001,
  FlatProgress = 1002,
  SplitDiscussionProgress = 1003,
}


const enum ShowAuthorHow {
  UsernameOnly = 1,
  UsernameThenFullName = 2,
  FullNameThenUsername = 3,  // the default
}


const enum TopicSortOrder {
  BumpTime = 1,
  CreatedAt = 2,
  ScoreAndBumpTime = 3,
  // LikesAndBumpTime, — perhaps add back later?
}


const enum TopTopicsPeriod {
  Day = 1,
  Week = 2,
  Month = 3,
  Quarter = 4,
  Year = 5,
  All = 6
}

type PageType = PageRole;

const enum PageRole { // dupl in client/e2e/test-types.ts [5F8KW0P2]  RENAME to PageType
  CustomHtmlPage = 1,
  WebPage = 2,  // rename to Info?
  Code = 3,
  SpecialContent = 4,
  EmbeddedComments = 5,
  Blog = 6,
  Forum = 7,
  About = 9,
  Question = 10,
  Problem = 14,
  Idea = 15,
  ToDo = 13,  // Only used briefly when creating new topics. Gets converted to Idea with status Planned.
  MindMap = 11,
  Discussion = 12,
  FormalMessage = 17,
  OpenChat = 18,
  PrivateChat = 19,
    // DirectMessage = 20,
  Form = 20,  // try to remove?
  Critique = 16, // [plugin]
  UsabilityTesting = 21, // [plugin]
}


// Sync with Scala [5KBF02].
const enum PageDoingStatus {
  Discussing = 1,
  Planned = 2,
  Started = 3,
  Done = 4,
  // Available = 5, ? Thinking about software:
  // sometimes I've implemented a feature, but not updated the server.
  // So, it's been "done" / implemented — but is not yet "available" to others.
  // Hmm, hmm, hmm. Or is this "Available" status a can of worms? What about
  // available in various backported branches? Would it be better for
  // the community to use tags (and tag values?) to indcate where the thing
  // has yet been made available?  ... Also, which icon, for Available o.O
  // cannot think of any make-sense icon. Better skip this (!). Stop at Done.
}


const enum PinPageWhere {
  InCategory = 1,
  Globally = 3,
}


const enum DiscussionLayout {
  Default = 0,
}

type NestingDepth = number;
const InfiniteNesting: NestingDepth = -1;  // sync with Scala

const enum PostSortOrder {
  Default = 0,
  BestFirst = 1,
  NewestFirst = 2,
  OldestFirst = 3,
  // Random = 4 ?
  // NewAndBestFirst = 5,
}

const enum ProgressLayout {
  Default = 0,
  Enabled = 1,
  MostlyDisabled = 2,
}

const enum OrigPostVotes {
  Default = 0,
  NoVotes = 1,
  LikeVotesOnly = 2,
  AllVotes = 3,
}



const enum SiteStatus {
  NoAdmin = 1,
  Active = 2,
  ReadAndCleanOnly = 3,
  HiddenUnlessStaff = 4,
  HiddenUnlessAdmin = 5,
  Deleted = 6,
  Purged = 7,
}


const enum TrustLevel {
  Stranger = 0,
  New = 1,
  Basic = 2,
  FullMember = 3,
  Trusted = 4,
  Regular = 5,
  CoreMember = 6,
}


const enum ThreatLevel {
  SuperSafe = 1,
  SeemsSafe = 2,
  HopefullySafe = 3,
  MildThreat = 4,
  ModerateThreat = 5,
  SevereThreat = 6,
}


const enum Groups {
  NoUserId = 0,
  EveryoneId = 10,
  AllMembersId = 11,
  BasicMembersId = 12,
  FullMembersId = 13,
  TrustedId = 14,
  RegularsId = 15,
  CoreMembersId = 16,
  StaffId = 17,
  ModeratorsId = 18,
  AdminsId = 19,
  MaxBuiltInGroupId = AdminsId,
}


const enum ContribAgreement {
  CcBy3And4 = 10,
  CcBySa3And4 = 40,
  CcByNcSa3And4 = 70,
  UseOnThisSiteOnly = 100
}

const enum ContentLicense {
  CcBy4 = 10,
  CcBySa4 = 40,
  CcByNcSa4 = 70,
  AllRightsReserved = 100
}


const enum HostRole {
  Canonical = 1,
  Redirect = 2,
  Link = 3,
  Duplicate = 4,
}


// vim: et ts=2 sw=2 tw=0 fo=r list
