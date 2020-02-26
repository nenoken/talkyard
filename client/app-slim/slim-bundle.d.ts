
/// <reference path="server-vars.d.ts" />
/// <reference path="model.ts" />
/// <reference path="translations.d.ts" />

declare const t: TalkyardTranslations;


// In constants.ts:

declare const ReactCSSTransitionGroup: any;
declare const ReactDOMFactories: any;

declare function createReactClass<P, S = {}>(spec: React.ComponentSpec<P, S>):
    React.ClassicComponentClass<P>;

declare function reactCreateFactory(x);
declare const rFragment: any;

declare function doNextFrameOrNow(x);
declare function getSetCookie(cookieName: string, value?: string, options?: any): string | null;
declare const parseQueryString: (s: string) => any;
declare const stringifyQueryString: (s: any) => string;

declare const ReactStartedClass: string;

declare const TooHighNumber: number;

declare const EmptyPageId: PageId;
declare const FirstSiteId: SiteId;

declare const NoId: number; // ?? won't work for page id
declare const NoCategoryId: CategoryId;
declare const NoPermissionId: number;

declare const NoPostId: PostId;
declare const TitleNr: PostNr;
declare const BodyNr: PostNr;
declare const BodyNrStr: string;
declare const FirstReplyNr: PostNr;
declare const NoDraftNr: number;

declare let NoUserId: UserId;
declare const SystemUserId: UserId;
declare const MinMemberId: UserId;
declare const LowestAuthenticatedUserId: UserId;
declare const LowestNormalMemberId: UserId;
declare const MaxUsernameLength: number;

declare const MaxGuestId: UserId;
declare const UnknownUserId: UserId;

declare const ReviewDecisionUndoTimoutSeconds: number;

declare function makeNoPageData(): MyPageData;
declare function makeAutoPage(): any;

declare const ManualReadMark;
declare const YellowStarMark;
declare const FirstStarMark;
declare const BlueStarMark;
declare const LastStarMark;

declare const MaxNumFirstPosts: number;

declare const MaxEmailsPerUser: number;

declare const IgnoreThisError: number;
declare const UseBeacon: string;


declare const SiteStatusStrings: string[];


declare const ApiUrlPathPrefix: string;
declare const UsersRoot: string;
declare const GroupsRoot: string;
declare const SearchRootPath: string;


declare const RoutePathLatest: string;
declare const RoutePathNew: string;
declare const RoutePathTop: string;
declare const RoutePathCategories: string;


declare const ImpersonationCookieName: string;


declare const UseWideForumLayoutMinWidth: number;
declare const UseWidePageLayoutMinWidth: number;
declare const WatchbarWidth: number;
declare const ContextbarMinWidth: number;

declare const ServerSideWindowWidth: number;

declare const FragActionAndReplyToPost: string;
declare const FragActionAndEditPost: string;
declare const FragActionHashComposeTopic: string;
declare const FragActionHashComposeMessage: string;
declare const FragActionHashScrollLatest: string;


declare namespace ed {
  namespace editor {
    namespace CdnLinkifyer {
      function replaceLinks(md: any): void;
    }
  }
}

// In other files:

declare namespace debiki2 {

  function getMainWin(): MainWin;
  function win_canUseCookies(win: MainWin): boolean;

  function getNowMs(): WhenMs;

  let iframeOffsetWinSize;

  function oneIfDef(x: any): number;

  function $first(selector: string): HTMLElement;
  function $all(selector: string): HTMLCollectionOf<HTMLElement>;
  function $byId(elemId: string): HTMLElement;
  function $$byClass(className: string): HTMLCollectionOf<Element>;
  const $h: any;

  function flashPostNrIfThere(nr: PostNr);

  // React-Router:
  const Router: any;
  const Switch: any;
  const Route: any;
  const Redirect: any;
  function RedirPath(props: RedirPathProps);
  function RedirToNoSlash({ path: string });
  function RedirAppend({ path, append });
  var Link; // ReactRouterDOM.Link
  var NavLink; // ReactRouterDOM.NavLink
  function LiNavLink(props, ...contents); // A NavLink in a <li>
  function LiExtLink(props, ...contents); // An <a href=...> in a <li>

  var createComponent: any;       // don't use — I'm renaming to createFactory
  var createClassAndFactory: any; // don't use — I'm renaming to createFactory
  function createFactory<P, S = any>(compSpec: React.ComponentSpec<P, S>): React.Factory<any>;


  function replaceById(itemsWithId: any[], replacement);
  function deleteById(itemsWithId: any[], id);

  namespace notfs {
    function PageNotfPrefButton(props: {
        target: PageNotfPrefTarget, store: Store, ownPrefs: OwnPageNotfPrefs,
        ppsById?: { [ppId: number]: Participant },
        saveFn?: (notfLevel: PageNotfLevel) => void });
  }

  namespace utils {
    function makeShowPostFn(currentPostNr: PostNr, postToShowNr: PostNr);
    var scrollIntoViewInPageColumn;
    function makeMountNode();
    var DropdownModal;
    var ModalDropdownButton;
    var FadeInOnClick;

    function maybeRunTour(tour: TalkyardTour);
  }

  namespace util {
    var ExplainingListItem;
  }

  namespace help {
    var HelpMessageBox;
    function isHelpMessageClosedAnyVersion(store: Store, messageId: string): boolean;
  }

  namespace topbar {
    function getTopbarHeightInclShadow(): number;
    const TopBar: any;
  }

  namespace sidebar {
    const contextBar: {
      closeSidebar: () => void;
      openSidebar: () => void;
      showAdminGuide: () => void;
    }
  }

  // should be moved to inside the editor bundle
  namespace editor {
    var SelectCategoryDropdown;

    // from editor-bundle-not-yet-loaded.ts:
    function toggleWriteReplyToPostNr(postNr: PostNr, inclInReply: boolean, anyPostType?: number);
    function openToEditPostNr(postNr: PostNr, onDone?);
    function editNewForumPage(categoryId: CategoryId, role: PageRole);
    function openToEditChatTitleAndPurpose();
    function openToWriteChatMessage(text: string, onDone);
    function openToWriteMessage(userId: UserId);
  }

  namespace login {
    var anyContinueAfterLoginCallback;
    function continueAfterLogin(anyReturnToUrl?: string);
    function loginIfNeededReturnToAnchor(
        loginReason: LoginReason | string, anchor: string, success?: () => void, willCompose?: boolean);
    function loginIfNeededReturnToPost(
        loginReason: LoginReason | string, postNr: PostNr, success?: () => void, willCompose?: boolean);

    function loginIfNeeded(loginReason, returnToUrl: string, onDone?: () => void);
    function openLoginDialogToSignUp(purpose);
    function openLoginDialog(purpose);

    function makeSsoUrl(store: Store, returnToUrl: string): string;
  }

  function reactGetRefRect(ref): Rect;
  var Server: any;
  var StoreListenerMixin: any;

  // Currenty read-only (never call the returned 'setState'). Instead, ...
  function useStoreState(): [Store, () => void];
  // ... use ReactActions to update the store. For now. Would want to remove ReactActions,
  // and use only hooks instead? [4WG20ABG2]
  var ReactActions: any;

  var ReactStore: any;

  var findDOMNode: any;
  var die: any;
  var dieIf: any;
  var scrollToBottom: any;
  var prettyBytes: any;
  var Server: any;
  var reactelements: any;
  var hashStringToNumber: any;

  function stableStringify(obj: any): string;

  function canUseLocalStorage(): boolean;
  function putInLocalStorage(key: any, value: any);
  function putInSessionStorage(key: any, value: any);
  function getFromLocalStorage(key: any): any;
  function getFromSessionStorage(key: any): any;
  function removeFromLocalStorage(key);
  function removeFromSessionStorage(key);

  function event_isCtrlEnter(event): boolean;
  function event_isEscape(event): boolean;
  function page_isChat(pageRole: PageRole): boolean;
  function page_isPrivateGroup(pageRole: PageRole): boolean;
  function pageRole_iconClass(pageRole: PageRole): string;

  function me_uiPrefs(me: Myself): UiPrefs;
  function member_isBuiltIn(member: Member): boolean;
  function user_isSuspended(user: UserInclDetails, nowMs: WhenMs): boolean;
  function user_threatLevel(user: UserInclDetails): ThreatLevel;
  function user_trustLevel(user: Myself | UserInclDetails): TrustLevel;
  function user_isGone(user: Myself | BriefUser | UserInclDetails | ParticipantAnyDetails): boolean;

  function uppercaseFirst(text: string): string;
  function firstDefinedOf(x, y, z?): any;
  function groupByKeepOne<V>(vs: V[], fn: (v: V) => number): { [key: number]: V };
  function isNullOrUndefined(x): boolean;
  function isDefined2(x): boolean;  // = !_.isUndefined
  function nonEmpty(x): boolean;
  function isDigitsOnly(maybeDigits: string): boolean;
  function isBlank(x: string): boolean;

  function whenMsToIsoDate(whenMs: number): string;

  function seemsSelfHosted(): boolean;
  function isInSomeEmbCommentsIframe(): boolean;
  function isBlogCommentsSite(): boolean;
  function isCommunitySite(): boolean;

  var isWikiPost;
  var isStaff;
  function user_isTrustMinNotThreat(me: UserInclDetails | Myself, trustLevel: TrustLevel): boolean;
  var threatLevel_toString;
  var isGuest;
  var user_isGuest;
  function store_maySendDirectMessageTo(store: Store, user: UserInclDetails): boolean;
  var page_isGroupTalk;

  function store_getAuthorOrMissing(store: Store, post: Post): BriefUser;
  function store_getUserOrMissing(store: Store, userId: UserId, errorCode2?: string): BriefUser;
  var store_thisIsMyPage;

  function draftType_toPostType(draftType: DraftType): PostType | undefined;
  function postType_toDraftType(postType: PostType): DraftType | undefined;
  function store_findTheDefaultCategory(store: Store): Category | undefined;
  function store_ancestorsCategoriesCurrLast(store: Store, categoryId: CategoryId): Category[];
  function store_findCatsWhereIMayCreateTopics(store: Store): Category[];

  function page_makePostPatch(page: Page, post: Post): StorePatch;
  function store_makeDraftPostPatch(store: Store, page: Page, draft: Draft): StorePatch;

  function post_makePreviewIdNr(parentPostNr: PostNr, newPostType: PostType): PostNr & PostId;

  function store_makeNewPostPreviewPatch(
      store: Store, page: Page, parentPostNr: PostNr, safePreviewHtml: string,
      newPostType?: PostType): StorePatch;
  function store_makeEditsPreviewPatch(
      store: Store, page: Page, post: Post, safePreviewHtml: string): StorePatch;
  function store_makeDeletePreviewPostPatch(
      store: Store, parentPostNr: PostNr, newPostType?: PostType): StorePatch;

  var hasErrorCode;
  var page_mayChangeRole;
  function page_canToggleClosed(page: Page): boolean;
  function store_maySendInvites(store: Store, user: Myself | UserInclDetails): MayMayNot;
  var isMember;
  var userId_isGuest;
  function store_isNoPage(store: Store): boolean;
  function store_isPageDeleted(store: Store): boolean;
  function store_canDeletePage(store: Store): boolean;
  function store_canUndeletePage(store: Store): boolean;
  function store_canPinPage(store: Store): boolean;
  function siteStatusToString(siteStatus: SiteStatus);
  var cloneRect;
  var cloneEventTargetRect;

  function origin(): string;
  function linkToPageId(pageId: PageId): string;
  function linkToPostNr(pageId: PageId, postNr: PostNr): string;
  function linkToDraftSource(draft: Draft, pageId?: PageId, postNr?: PostNr): string;
  function linkToNotificationSource(notf: Notification): string;
  function linkToAdminPageAdvancedSettings(hostname?: string): string;
  function linkToRedirToAboutCategoryPage(categoryId: CategoryId): string;
  function linkToUserInAdminArea(user: Myself | UserInclDetails | Participant | UserId): string;
  function linkToSendMessage(idOrUsername: UserId | string): string;
  function linkToUserInAdminArea(userId: UserId): string;
  function linkToUserProfilePage(idOrUsername: Myself | Participant | UserId | string): string;
  function pathTo(user: Participant | Myself | UserId | string): string;
  function linkToUsersNotfs(userIdOrUsername: UserId | string): string;
  function linkToMembersNotfPrefs(userIdOrUsername: UserId | string): string;
  function linkToSendMessage(userIdOrUsername: UserId | string): string;
  function linkToInvitesFromUser(userId: UserId): string;
  function linkToUsersEmailAddrs(userIdOrUsername: UserId | string): string;
  function linkToAdminPage(): string;
  function linkToAdminPageLoginSettings(): string;
  function linkToAdminPageModerationSettings(): string;
  function linkToAdminPageEmbeddedSettings(): string;
  function linkToReviewPage(): string;
  function linkToStaffInvitePage(): string;
  function externalLinkToAdminHelp(): string;
  function linkToGroups(): string;
  function linkToMyDraftsEtc(store: Store): string;
  function linkToMyProfilePage(store: Store): string;
  function linkToUpload(origins: Origins, uploadsPath: string): string;
  function linkToResetPassword(): string;

  var anyForbiddenPassword;

  function isSection(pageRole: PageRole): boolean;
  function page_isClosedNotDone(page: Page): boolean;
  function page_hasDoingStatus(page: Page): boolean;
  function page_canChangeCategory(page: Page): boolean;
  function page_mostRecentPostNr(page: Page): number;

  function settings_showCategories(settings: SettingsVisibleClientSide, me: Myself): boolean;
  function settings_showFilterButton(settings: SettingsVisibleClientSide, me: Myself): boolean;
  function settings_showTopicTypes(settings: SettingsVisibleClientSide, me: Myself): boolean;
  function settings_selectTopicType(settings: SettingsVisibleClientSide, me: Myself): boolean;


  function timeExact(whenMs: number, clazz?: string);

  namespace avatar {
    var Avatar;
  }

  function pageNotfPrefTarget_findEffPref(target: PageNotfPrefTarget, store: Store, ownPrefs: OwnPageNotfPrefs): EffPageNotfPref;
  function notfPref_title(notfPref: EffPageNotfPref): string;
  function notfLevel_descr(notfLevel: PageNotfLevel, effPref: EffPageNotfPref, ppsById: PpsById): any;
  function makeWhyNotfLvlInheritedExpl(effPref: EffPageNotfPref, ppsById: PpsById);

  namespace edithistory {

  }
  namespace help {

  }

  namespace forum {
    var TopicsList;
  }

  namespace page {
    var Post;
    namespace Hacks {
      function processPosts(startElemId?: string);
    }
  }
  namespace pagedialogs {
    function getServerErrorDialog(): any;
    function showAndThrowClientSideError(errorMessage: string);
    var openSharePopup;
    const Facebook;
    const Twitter;
    const Google;
    const LinkedIn;
    const Email;
  }

  var SelectCategoryDropdown: any;

  // From widgets.ts:
  var PrimaryButton;
  var Button;
  var PrimaryLinkButton;
  var ExtLinkButton;
  var LinkUnstyled;
  var LinkButton;
  var InputTypeSubmit; // could move to more-bundle.js, but is just 1 line
  var MenuItem;
  var MenuItemLink;
  var MenuItemsMany;
  var MenuItemDivider;
  function UserName(props: {
    user: BriefUser, store: Store, makeLink?: boolean, onClick?: any, avoidFullName?: boolean });
  var FacebookLogoImage;

  // More stuff, place where?
  //namespace reactelements {
  //  var NameLoginBtns;
  //}

  // From oop-methods.ts:
  function userStats_totalNumPosts(stats: UserStats): number;
  function userStats_totalNumPostsRead(stats: UserStats): number;
  function trustLevel_toString(trustLevel: TrustLevel): string;

}

// vim: et ts=2 sw=2 fo=r list
