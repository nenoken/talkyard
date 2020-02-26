/**
 * Copyright (C) 2012-2013 Kaj Magnus Lindberg (born 1979)
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

package com.debiki.core

import com.debiki.core.Prelude._
import com.debiki.core.PageParts.MaxTitleLength
import java.{util => ju}
import scala.collection.mutable



/** A Page can be a blog post, a forum topic, a forum topic list, a Wiki page,
  * a Wiki main page, or a site's homepage, for example.
  */
// REFACTOR  combine Page and PageParts into the same trait, "Page".[ONEPAGEDAO]
//  + move to talkyard-server,  not needed here in ty-core.
trait Page {

  def id: PageId
  def siteId: SiteId
  def categoryId: Option[CategoryId] = meta.categoryId
  def pageType: PageType = meta.pageType
  def meta: PageMeta
  def path: Option[PagePathWithId]
  def parts: PageParts
  def version: PageVersion

  def anyAnswerPost: Option[Post] = {
    meta.answerPostId.flatMap(parts.postById)
  }

}


object Page {

  def isOkayId(id: String): Boolean =
    id forall { char =>
      def isLower = 'a' <= char && char <= 'z'
      def isUpper = 'A' <= char && char <= 'Z'
      def isDigit = '0' <= char && char <= '9'
      isDigit || isLower || isUpper || char == '_'
    }
}



/** A page that does not know what it contains (the `parts` fields is absent).
  */
case class PagePathAndMeta(
  path: PagePath,
  meta: PageMeta) {

  def id: PageId = meta.pageId
  def pageId: PageId = meta.pageId
  def categoryId: Option[CategoryId] = meta.categoryId
  def pageType: PageType = meta.pageType

  requireMetaMatchesPaths(this)
}



/** Helper function that checks that page meta and page path IDs matches. */
object requireMetaMatchesPaths {
  def apply(page: {
    def meta: PageMeta
    def path: PagePath
  }) {
    if (page.path.pageId.isDefined) require(page.meta.pageId == page.path.pageId.get)
    else require(page.meta.pageId == "?")
  }
}



object PageMeta {

  def forNewPage(
        pageId: PageId,
        pageRole: PageType,
        authorId: UserId,
        creationDati: ju.Date,  // RENAME to createdAt
        numPostsTotal: Int,
        extId: Option[ExtImpId] = None,
        layout: Option[PageLayout] = None,
        plannedAt: Option[ju.Date] = None,
        deletedAt: Option[When] = None,
        pinOrder: Option[Int] = None,
        pinWhere: Option[PinPageWhere] = None,
        categoryId: Option[CategoryId] = None,
        embeddingUrl: Option[String] = None,
        hidden: Boolean = false,
        publishDirectly: Boolean = false): PageMeta = {
    var result = PageMeta(
      pageId = pageId,
      extImpId = extId,
      pageType = pageRole,
      version = 1,
      createdAt = creationDati,
      plannedAt = plannedAt,
      updatedAt = creationDati,
      publishedAt = if (publishDirectly) Some(creationDati) else None,
      deletedAt = deletedAt.map(_.toJavaDate),
      categoryId = categoryId,
      embeddingPageUrl = embeddingUrl,
      authorId = authorId,
      layout = layout getOrElse PageLayout.Default,
      pinOrder = pinOrder,
      pinWhere = pinWhere,
      numLikes = 0,
      numWrongs = 0,
      numBurys = 0,
      numUnwanteds = 0,
      numRepliesVisible = 0,
      numRepliesTotal = 0,
      numPostsTotal = numPostsTotal,
      numChildPages = 0)
    if (hidden) {
      result = result.copy(hiddenAt = Some(When.fromDate(result.createdAt)))
    }
    result
  }

  val MinPinOrder = 1
  val MaxPinOrder = 100
  def isOkPinOrder(order: Int) = MinPinOrder <= order && order <= MaxPinOrder
}


/** @param pageId
  * @param pageType
  * @param createdAt
  * @param updatedAt
  * @param publishedAt
  * @param bumpedAt
  * @param lastApprovedReplyAt
  * @param lastApprovedReplyById Set to None if there's no reply.
  * @param categoryId
  * @param embeddingPageUrl The canonical URL to the page, useful when linking to the page.
  *            Currently only needed and used for embedded comments, and then it
  *            is the URL of the embedding page.
  * @param authorId
  * @param frequentPosterIds: Most frequent poster listed first. Author & last-reply-by excluded.
  * @param layout: A bitmask that tells JS code how to render the page
  * @param numLikes
  * @param numWrongs
  * @param numBurys
  * @param numUnwanteds
  * @param numRepliesVisible Replies that haven't been deleted or hidden, and have been approved.
  *                          Includes collapsed and closed replies.
  * @param numRepliesTotal Counts all replies, also deleted, hidden and not-yet-approved replies.
  * @param numPostsTotal Includes all replies, and also meta message posts.
  * @param answeredAt For questions: when a reply was accepted as the answer to the question.
  * @param answerPostId The id of the post that answers this question.
  // [befrel] @param answerPostNr
  * @param plannedAt When a problem/idea got planned to be fixed/done.
  * @param startedAt When started fixing/implementing the problem/idea.
  * @param doneAt When a problem/idea/todo was done, e.g. when bug fixed or idea implemented.
  * @param closedAt When the topic was closed, e.g. if a question was off-topic or idea rejected.
  * @param lockedAt When locked so no new replies can be added.
  * @param frozenAt When frozen, so cannot be changed in any way at all (not even edits).
  * @param hiddenAt E.g. all posts flagged & hidden, so nothing to see. Or page not yet approved.
  * @param htmlHeadTitle Text for the html <title>...</title> tag.
  * @param htmlHeadDescription Text for the html <description content"..."> tag.
  */
case class PageMeta( // ?RENAME to Page? And rename Page to PageAndPosts?  [exp] ok use. Missing, fine: num_replies_to_review  incl_in_summaries  wait_until
  pageId: String,
  extImpId: Option[ExtImpId] = None,
  pageType: PageType,
  version: PageVersion,
  createdAt: ju.Date,
  updatedAt: ju.Date,
  publishedAt: Option[ju.Date] = None,
  bumpedAt: Option[ju.Date] = None,
  lastApprovedReplyAt: Option[ju.Date] = None,
  lastApprovedReplyById: Option[UserId] = None,
  categoryId: Option[CategoryId] = None,
  embeddingPageUrl: Option[String],
  authorId: UserId,
  frequentPosterIds: Seq[UserId] = Seq.empty,
  // REFACTOR move to site settings and admin area
  layout: PageLayout = PageLayout.Default,
  pinOrder: Option[Int] = None,
  pinWhere: Option[PinPageWhere] = None,
  numLikes: Int = 0,
  numWrongs: Int = 0,
  numBurys: Int = 0,
  numUnwanteds: Int = 0,
  numRepliesVisible: Int = 0,
  numRepliesTotal: Int = 0,
  numPostsTotal: Int = 0,
  numOrigPostLikeVotes: Int = 0,
  numOrigPostWrongVotes: Int = 0,
  numOrigPostBuryVotes: Int = 0,
  numOrigPostUnwantedVotes: Int = 0,
  numOrigPostRepliesVisible: Int = 0,
  // ? Refactor: Change to enums. Remove timestamps (not used anyway). See model.ts [5RKT02].
  answeredAt: Option[ju.Date] = None,
  answerPostId: Option[PostId] = None,
  plannedAt: Option[ju.Date] = None,
  startedAt: Option[ju.Date] = None,
  doneAt: Option[ju.Date] = None,
  closedAt: Option[ju.Date] = None,
  lockedAt: Option[ju.Date] = None,
  frozenAt: Option[ju.Date] = None,
  // unwantedAt: Option[ju.Date] = None, -- when enough core members voted Unwanted
  hiddenAt: Option[When] = None,
  deletedAt: Option[ju.Date] = None,
  htmlTagCssClasses: String = "",  // try to move to EditedSettings, so will be inherited
  htmlHeadTitle: String = "",
  htmlHeadDescription: String = "",
  numChildPages: Int = 0) { // <-- CLEAN_UP remove, replace with category table


  extImpId.flatMap(Validation.findExtIdProblem) foreach { problem =>
    throwIllegalArgument("TyE5KT3RUD0", s"Bad page extId: $problem")
  }

  private def wp = s"page id: '$pageId', ext id: '$extImpId'"  // "which page"

  require(lastApprovedReplyAt.isDefined == lastApprovedReplyById.isDefined, s"[DwE5JGY1] $wp")

  BUG // when importing blog comments from elsewhere, they might have been creted
  // before the Talkyard page, and can have earlier replied-at tmestamps.
  // (They were posted earlier, but not created *in Talkyard* until later.)
  SHOULD // remove this constraint? And also the dw1_pages_createdat_replyat__c_le
  // database constraint.
  // Real solution: Split this timestamp into two:  writtenAt, and insertedAt.
  // And, writtenAt can be older than the page creation time, and is what's shown
  // as written-at date on the html page.  Whilst  insertedAt must be more recent
  // than the page, and is what's used for sorting comments so one finds those
  // one hasn't read yet. (Like Git's authored-at date and commited-at dates.)
  // See this branch:  b6fb20d4880cbf2c861  "W author & insertion date."
  //
  require(lastApprovedReplyAt.forall(_.getTime >= createdAt.getTime), s"[TyE7WKG2AG4] $wp")

  require(updatedAt.getTime >= createdAt.getTime, s"[TyE7WKG05KS] $wp")
  require(publishedAt.forall(_.getTime >= createdAt.getTime), s"[TyE8GK405KS] $wp")
  require(bumpedAt.forall(_.getTime >= createdAt.getTime), s"[TyE0NFATI3D] $wp")
  // If there are no replies, then there are no frequent posters.
  require(lastApprovedReplyById.isDefined || frequentPosterIds.isEmpty, s"[TyE306HMSJ24] $wp")
  require(frequentPosterIds.length <= 3, s"[DwE6UMW3] $wp") // for now — change if needed

  require(version > 0, s"[DwE6KFU2] $wp")
  require(pageType != PageType.AboutCategory || categoryId.isDefined, s"[DwE5PKI8] $wp")
  require(!pinOrder.exists(!PageMeta.isOkPinOrder(_)), s"[DwE4kEYF2] $wp")
  require(pinOrder.isEmpty == pinWhere.isEmpty, s"[DwE36FK2] $wp")
  require(numLikes >= 0, s"[DwE6PKF3] $wp")
  require(numWrongs >= 0, s"[DwE9KEFW2] $wp")
  require(numBurys >= 0, s"[DwE2KEP4] $wp")
  require(numUnwanteds >= 0, s"[DwE4JGY7] $wp")
  require(numPostsTotal >= numRepliesTotal, s"Fail: $numPostsTotal >= $numRepliesTotal [EdE2WTK4L] $wp")
  require(numOrigPostLikeVotes >= 0, s"[DwE5KJF2] $wp")
  require(numOrigPostLikeVotes <= numLikes, s"Fail: $numOrigPostLikeVotes <= $numLikes [EdE5KJF2B] $wp")
  require(numOrigPostWrongVotes >= 0, s"[DwE4WKEQ1] $wp")
  require(numOrigPostWrongVotes <= numWrongs, s"Fail: $numOrigPostWrongVotes <= $numWrongs [EdE4WKEQ1B] $wp")
  require(numOrigPostBuryVotes >= 0, s"[DwE8KGY4] $wp")
  require(numOrigPostBuryVotes <= numBurys, s"Fail: $numOrigPostBuryVotes <= $numBurys [EdE8KGY4B] $wp")
  require(numOrigPostUnwantedVotes >= 0, s"[DwE0GFW8] $wp")
  require(numOrigPostUnwantedVotes <= numUnwanteds,
    s"Fail: $numOrigPostUnwantedVotes <= $numUnwanteds [EdE4GKY8] $wp")
  require(numOrigPostRepliesVisible >= 0, s"[DwE0GY42] $wp")
  require(numOrigPostRepliesVisible <= numRepliesVisible,
    s"Fail: $numOrigPostRepliesVisible <= $numRepliesVisible [EsE0GY42B] $wp")
  //require(numRepliesVisible >= 0, "DwE6KPE78") - bug in PostsDao.changePostStatus()?
  require(numRepliesTotal >= numRepliesVisible,
    s"Fail: $numRepliesTotal >= $numRepliesVisible [DwE4REQ2] $wp")
  //require(numChildPages >= 0, "DwE8KPEF0") -- oops fails, not so very important, for now instead:
  require(answeredAt.isEmpty || createdAt.getTime <= answeredAt.get.getTime, s"[DwE4KG22] $wp")
  require(plannedAt.isEmpty || createdAt.getTime <= plannedAt.get.getTime, s"[DwE0FUY2] $wp")
  require(startedAt.isEmpty || createdAt.getTime <= startedAt.get.getTime, s"[DwE5JRQ0] $wp")
  require(doneAt.isEmpty || createdAt.getTime <= doneAt.get.getTime, s"[DwE4PUG2] $wp")
  require(closedAt.isEmpty || createdAt.getTime <= closedAt.get.getTime, s"[DwE7KPE8] $wp")
  require(lockedAt.isEmpty || createdAt.getTime <= lockedAt.get.getTime, s"[DwE3KWV6] $wp")
  require(frozenAt.isEmpty || createdAt.getTime <= frozenAt.get.getTime, s"[DwE4YUF8] $wp")
  require(doneAt.isEmpty || !plannedAt.exists(_.getTime > doneAt.get.getTime), s"[DwE6K8PY2] $wp")
  require(doneAt.isEmpty || !startedAt.exists(_.getTime > doneAt.get.getTime), s"[EdE6K8PY3] $wp")
  // A topic that has been fixed or solved, should be in the closed state. [5AKBS2]
  require((doneAt.isEmpty && answeredAt.isEmpty) || closedAt.isDefined, s"[TyE5AKBS2] $wp")
  // A locked or frozen topic, should be closed too.
  require((lockedAt.isEmpty && frozenAt.isEmpty) || closedAt.isDefined, s"[DwE6UMP3] $wp")
  require(answeredAt.isEmpty == answerPostId.isEmpty, s"[DwE2PYU5] $wp")
  require(numChildPages >= 0, s"Page $pageId has $numChildPages child pages [EsE5FG3W02] $wp")

  def isPinned: Boolean = pinOrder.isDefined
  def isClosed: Boolean = closedAt.isDefined
  def isVisible: Boolean = hiddenAt.isEmpty && deletedAt.isEmpty
  def isHidden: Boolean = hiddenAt.isDefined
  def isDeleted: Boolean = deletedAt.isDefined

  def isGroupTalk: Boolean = pageType.isGroupTalk
  def isPrivateGroupTalk: Boolean = pageType.isPrivateGroupTalk

  def isChatPinnedGlobally: Boolean =
    pageType == PageType.OpenChat && pinWhere.contains(PinPageWhere.Globally)

  def status: PageStatus =   // RENAME to publishedStatus
    if (publishedAt.isDefined) PageStatus.Published
    else PageStatus.Draft

  def doingStatus: PageDoingStatus = {
    if (doneAt.isDefined) PageDoingStatus.Done
    else if (startedAt.isDefined) PageDoingStatus.Started
    else if (plannedAt.isDefined) PageDoingStatus.Planned
    else PageDoingStatus.Discussing
  }

  def bumpedOrPublishedOrCreatedAt: ju.Date = bumpedAt orElse publishedAt getOrElse createdAt

  def addUserIdsTo(ids: mutable.Set[UserId]) {
    ids += authorId
    ids ++= frequentPosterIds
    lastApprovedReplyById.foreach(ids += _)
  }

  def idVersion = PageIdVersion(pageId, version = version)


  def copyWithNewVersion: PageMeta = copy(version = version + 1)


  def copyWithNewRole(newRole: PageType): PageMeta = {
    var newClosedAt = closedAt
    val (newAnsweredAt, newAnswerPostUniqueId) = newRole match {
      case PageType.Question => (answeredAt, answerPostId)
      case _ =>
        if (answeredAt.isDefined) {
          // Reopen it since changing type.
          newClosedAt = None
        }
        (None, None)
    }

    val (newPlannedAt, newStartedAt, newDoneAt) = newRole match {
      case PageType.Problem | PageType.Idea =>
        // These page types understand planned/started/done, so keep them unchanged.
        (plannedAt, startedAt, doneAt)
      case _ =>
        // Other page types cannot be in planned/started/done statuses, so clear those fields.
        // However, if the topic is currently planned/started/done, then, when clearing
        // those fields, the topic might end up as Closed, and then the Closed icon gets shown,
        // which is confusing, and probably the topic was never just closed. — So clear closedAt.
        if (plannedAt.isDefined || startedAt.isDefined || doneAt.isDefined) {
          newClosedAt = None
        }
        (None, None, None)
    }

    copy(
      pageType = newRole,
      answeredAt = newAnsweredAt,
      answerPostId = newAnswerPostUniqueId,
      plannedAt = newPlannedAt,
      startedAt = newStartedAt,
      doneAt = newDoneAt,
      closedAt = newClosedAt)
  }

  def copyWithNewDoingStatus(newDoingStatus: PageDoingStatus, when: When): PageMeta = {
    // For now. Later, change  plannedAt, startedAt  etc, to just a doingStatus field,
    // with no timestamp? [5RKT02]
    def someWhen = Some(when.toJavaDate)
    val (newPlannedAt, newStartedAt, newDoneAt, newClosedAt) = newDoingStatus match {
      case PageDoingStatus.Discussing => (None, None, None, None)
      case PageDoingStatus.Planned => (someWhen, None, None, None)
      case PageDoingStatus.Started => (plannedAt, someWhen, None, None)
      case PageDoingStatus.Done =>
        // A page gets closed, when status changes to Done. [5AKBS2]
        (plannedAt, startedAt, someWhen, closedAt.orElse(someWhen))
    }

    copy(
      plannedAt = newPlannedAt,
      startedAt = newStartedAt,
      doneAt = newDoneAt,
      closedAt = newClosedAt)
  }

  def copyWithUpdatedStats(page: Page): PageMeta = {
    val body = page.parts.body
    def bodyVotes(fn: Post => Int): Int = body.map(fn) getOrElse 0

    var newMeta = copy(
      bumpedAt = When.anyJavaDateLatestOf(
        bumpedAt, page.parts.lastVisibleReply.map(_.createdAt)),
      lastApprovedReplyAt = page.parts.lastVisibleReply.map(_.createdAt),
      lastApprovedReplyById = page.parts.lastVisibleReply.map(_.createdById),
      frequentPosterIds = page.parts.frequentPosterIds,
      numLikes = page.parts.numLikes,
      numWrongs = page.parts.numWrongs,
      numBurys = page.parts.numBurys,
      numUnwanteds = page.parts.numUnwanteds,
      numRepliesVisible = page.parts.numRepliesVisible,
      numRepliesTotal = page.parts.numRepliesTotal,
      numPostsTotal = page.parts.numPostsTotal,
      numOrigPostLikeVotes = bodyVotes(_.numLikeVotes),
      numOrigPostWrongVotes = bodyVotes(_.numWrongVotes),
      numOrigPostBuryVotes = bodyVotes(_.numBuryVotes),
      numOrigPostUnwantedVotes = bodyVotes(_.numUnwantedVotes),
      numOrigPostRepliesVisible = page.parts.numOrigPostRepliesVisible,
      answeredAt = page.anyAnswerPost.map(_.createdAt),
      answerPostId = page.anyAnswerPost.map(_.id),
      version = page.version + 1)

    newMeta
  }

}


case class SimplePagePatch(
  extId: ExtImpId,
  pageType: Option[PageType],
  categoryRef: Option[Ref],
  authorRef: Option[Ref],
  title: String,
  body: String
  // later: bodyMarkupLang: Option[MarkupLang]
  ) {

  throwIllegalArgumentIf(title.isEmpty, "TyE306GXF24", "Page title is empty")
  throwIllegalArgumentIf(title.length > MaxTitleLength, "TyE5qDKWQJ6", "Title too long")
  throwIllegalArgumentIf(body.isEmpty, "TyE306GXF25", "Page body is empty")
  Validation.findExtIdProblem(extId) foreach { problem =>
    throwIllegalArgument("TyE8FKDXT2", s"Bad page extId: $problem")
  }
}



sealed abstract class PageType(
  protected val IntValue: Int,
  val staffOnly: Boolean = true /*[2GKW0M]*/) {

  /** True if this page is e.g. a blog or a forum — they can have child pages
    * (namely blog posts, forum topics).
    */
  def isSection: Boolean = false

  def isChat: Boolean = false

  /** If the topic is a discussion between a closed group of people, and visible only to them.
    */
  def isPrivateGroupTalk: Boolean = false

  /** If one needs to join the page before one can say anything.
    */
  def isGroupTalk: Boolean = isChat || isPrivateGroupTalk

  // Also see [WHENFOLLOW].
  def shallFollowLinks: Boolean = false

  def canClose: Boolean = !isSection

  def canHaveReplies = true

  // Sync with JS [6KUW204]
  def mayChangeRole: Boolean = true

  def hasDoingStatus: Boolean = false

  def toInt: Int = IntValue

  dieIf(isSection && mayChangeRole, "EsE7KUP2")

}


object PageType {

  def InfoPageMaxId: Int = WebPage.toInt

  case object CustomHtmlPage extends PageType(1) {
    // Only staff can create these — so ok to follow links.
    override def shallFollowLinks = true
    override def canHaveReplies = false
  }

  case object WebPage extends PageType(2) {
    // Only staff can create these — so ok to follow links.
    override def shallFollowLinks = true
  }

  case object Code extends PageType(3) {
    override def canHaveReplies = false // for now
    override def mayChangeRole = false
  }

  case object SpecialContent extends PageType(4) {
    override def canHaveReplies = false
    override def mayChangeRole = false
  }

  case object EmbeddedComments extends PageType(5, staffOnly = false)

  /** Lists blog posts. */
  case object Blog extends PageType(6) {
    override def isSection = true
    override def mayChangeRole = false
    override def canHaveReplies = false
  }

  /** Lists forum topics and categories. A Talkyard site can have many forum pages.
    * Each forum is then its own sub community, like a Reddit subreddit. */
  case object Forum extends PageType(7) {
    override def isSection = true
    override def mayChangeRole = false
    override def canHaveReplies = false
  }

  /** About a forum category (Discourse's forum category about topic). Shown as a per
    * category welcome page, and by editing the page body you edit the forum category
    * description. */
  case object AboutCategory extends PageType(9) {
    override def mayChangeRole = false
  }

  /** A question is considered answered when the author (or the staff) has marked some
    * reply as being the answer to the question. */
  case object Question extends PageType(10, staffOnly = false)

  /** Something that is broken and should be fixed. Can change status to Planned and Done. */
  case object Problem extends PageType(14, staffOnly = false) {
    override def hasDoingStatus = true
  }

  /** An idea about something to do, or a feature request. Can change status to Planned and Done. */
  case object Idea extends PageType(15, staffOnly = false) {
    override def hasDoingStatus = true
  }

  /** Something that's been planned, perhaps done, but perhaps not an Idea or Problem. */
  // [refactor] remove. Use Idea instead, bumped to "doing" state.
  case object ToDo extends PageType(13, staffOnly = false) {   // remove [4YK0F24]
    override def hasDoingStatus = true
  }

  /** Mind maps use 2D layout, even if the site is configured to use 1D layout. */
  case object MindMap extends PageType(11, staffOnly = false) {
    // May-edit permissions work differently for mind maps; don't let people change type to/from
    // mind map, because that could result in on-that-page permission escalation. [0JUK2WA5]
    BUG // harmless, but should hide page type mind-map in the js edit-title-category-&-type form,
    // so cannot accidentally attempt to change.
    override def mayChangeRole = false
  }

  /** For discussions (non-questions) or announcements or blog posts, for example.  */
  case object Discussion extends PageType(12, staffOnly = false)

  /** Any forum member with access to the page can join. */
  case object OpenChat extends PageType(18, staffOnly = false) {
    override def isChat = true
    override def mayChangeRole = false
  }

  /** Users added explicitly. Topic not shown in forum unless already member. */
  case object PrivateChat extends PageType(19, staffOnly = false) {
    override def isChat = true
    override def isPrivateGroupTalk = true
    override def canClose = false // lock them instead
    override def mayChangeRole = false
  }

  /** Formal direct messages between two users, or a group of users. Formal = not chat,
    * instead the full editor + preview are shown, so the sender is encouraged
    * to proofread and preview, rather than just typing lost of chatty verbose noise.
    *
    * If a FormalMessage topic is placed in a forum category, then everyone in a group
    * with the "correct" permissions on this category has access to the topic. (Not yet impl.)
    */
  case object FormalMessage extends PageType(17, staffOnly = false) {
    override def isPrivateGroupTalk = true
    override def canClose = false // lock them instead
    override def mayChangeRole = false
  }

  case object Form extends PageType(20, staffOnly = false)  // try to remove?

  case object Critique extends PageType(16, staffOnly = false) // [plugin] CLEAN_UP remove
  case object UsabilityTesting extends PageType(21, staffOnly = false) { // [plugin]
    override def hasDoingStatus = true
  }


  def fromInt(value: Int): Option[PageType] = Some(value match {
    case CustomHtmlPage.IntValue => CustomHtmlPage
    case WebPage.IntValue => WebPage
    case Code.IntValue => Code
    case SpecialContent.IntValue => SpecialContent
    case EmbeddedComments.IntValue => EmbeddedComments
    case Blog.IntValue => Blog
    case Forum.IntValue => Forum
    case AboutCategory.IntValue => AboutCategory
    case Question.IntValue => Question
    case Problem.IntValue => Problem
    case Idea.IntValue => Idea
    case ToDo.IntValue => ToDo
    case MindMap.IntValue => MindMap
    case Discussion.IntValue => Discussion
    case FormalMessage.IntValue => FormalMessage
    case OpenChat.IntValue => OpenChat
    case PrivateChat.IntValue => PrivateChat
    case Form.IntValue => Form
    case Critique.IntValue => Critique
    case UsabilityTesting.IntValue => UsabilityTesting
    //case WikiMainPage.IntValue => WikiMainPage
    //case WikiPage.IntValue => WikiPage
    case _ => return None
  })

}


trait PageLayout { def toInt: Int }  // REMOVE, and split into 3 fields, see: SiteSectionPageLayout
object PageLayout {
  object Default extends PageLayout { val toInt = 0 }

  def fromInt(value: Int): Option[PageLayout] = {
    if (TopicListLayout.MinIntVal <= value && value <= TopicListLayout.MaxIntVal) {
      TopicListLayout.fromInt(value)
    }
    else if (value == DiscussionLayout.Default.toInt || value == Default.toInt) {
      Some(PageLayout.Default)
    }
    else {
      None
    }
  }
}


/** A site section is e.g. a forum page — and you can view the contents of the forum,
  * in different ways:
  *
  * - Listing topics
  * - Listing categories
  * - A knowledge-base style search page  (not implemented)
  *
  * What's the best default view, depends on the community. People can click
  * buttons: "View categories", "View topic list", to switch between views.
  */
trait SiteSectionPageLayout



sealed abstract class CategoriesLayout(val IntVal: Int) extends PageLayout with  SiteSectionPageLayout {
  def toInt: Int = IntVal
}

object CategoriesLayout {
  val Default: CategoriesLayout = new CategoriesLayout(0) {}

  def fromInt(value: Int): Option[CategoriesLayout] = Some(value match {
    case Default.IntVal => Default
    case _ => return None
  })
}


sealed abstract class KnowledgeBaseLayout(val IntVal: Int) extends PageLayout {
  def toInt: Int = IntVal
}


sealed abstract class TopicListLayout(val IntVal: Int) extends PageLayout {
  def toInt: Int = IntVal
}

object TopicListLayout {
  object Default extends TopicListLayout(0)

  val MinIntVal = 1
  object TitleOnly extends TopicListLayout(1)
  object TitleExcerptSameLine extends TopicListLayout(2)
  object ExcerptBelowTitle extends TopicListLayout(3)
  object ThumbnailLeft extends TopicListLayout(4)
  object ThumbnailsBelowTitle extends TopicListLayout(5)
  object NewsFeed extends TopicListLayout(6)

  val MaxIntVal = 100

  def fromInt(value: Int): Option[TopicListLayout] = Some(value match {
    case Default.IntVal => Default
    case TitleOnly.IntVal => TitleOnly
    case TitleExcerptSameLine.IntVal => TitleExcerptSameLine
    case ExcerptBelowTitle.IntVal => ExcerptBelowTitle
    case ThumbnailLeft.IntVal => ThumbnailLeft
    case ThumbnailsBelowTitle.IntVal => ThumbnailsBelowTitle
    case NewsFeed.IntVal => NewsFeed
    case _ => return None
  })
}


sealed abstract class DiscussionLayout(val IntVal: Int) extends PageLayout {
  def toInt: Int = IntVal
}

object DiscussionLayout {
  object Default extends DiscussionLayout(0)
  def fromInt(value: Int): Option[DiscussionLayout] = Some(value match {
    case Default.IntVal => Default
    case _ => return None
  })
}


/* Old, instead, use  discPostNesting: Int
 *
object DiscussionLayout {
  val MinIntVal = 1001

  /** Threaded layout — Talkyard's default. Reddit, Hacker News, Disqus use this. */
  object Threaded extends DiscussionLayout(1001)

  /** Flat layout. Discourse, phpBB and other forum software use this. */
  object Flat extends DiscussionLayout(1002)

  /** Each top level reply has its own flat sub discussion. That is, one level nesting.
    * Facebook and StackOverflow uses this.
    */
  object ThreadedFlat extends DiscussionLayout(1003)


  val MaxIntVal = 1100

  def fromInt(value: Int): Option[DiscussionLayout] = Some(value match {
    case Default.IntVal => Default
    case Threaded.IntVal => Threaded
    case Flat.IntVal => Flat
    case ThreadedFlat.IntVal => ThreadedFlat
    case _ => return None
  })
}  */


sealed abstract class ProgressLayout(val IntVal: Int) {
  def toInt: Int = IntVal
}

object ProgressLayout {
  object Default extends ProgressLayout(0)
  object Enabled extends ProgressLayout(1)
  object MostlyDisabled extends ProgressLayout(2)

  def fromInt(value: Int): Option[ProgressLayout] = Some(value match {
    case Default.IntVal => Default
    case Enabled.IntVal => Enabled
    case MostlyDisabled.IntVal => MostlyDisabled
    case _ => return None
  })
}


/**
 * The page status, see debiki-for-developers.txt #9vG5I.
 */
sealed abstract class PageStatus
object PageStatus {  // RENAME to PagePublStatus — because there's also e.g. planned/doing/done statuses.
  // COULD rename to PrivateDraft, becaus ... other pages with limited
  // visibility might be considered Drafts (e.g. pages submitted for review).
  case object Draft extends PageStatus
  //COULD rename to Normal, because access control rules might result in
  // it effectively being non-pulbished.
  case object Published extends PageStatus

  case object Deleted extends PageStatus
  val All = List(Draft, Published, Deleted)

  def parse(text: String): PageStatus = text match {
    case "Draft" => Draft
    case "Published" => Published
    case "Deleted" => Deleted
    case x => illArgErr("DwE3WJH7", s"Bad page status: `$x'")
  }
}


// Sync with Typescript [5KBF02].
sealed abstract class PageDoingStatus(val IntVal: Int) { def toInt: Int = IntVal }
object PageDoingStatus {
  case object Discussing extends PageDoingStatus(1) { override def toString = "New" }
  case object Planned extends PageDoingStatus(2)
  case object Started extends PageDoingStatus(3)
  case object Done extends PageDoingStatus(4)
  // PostponedTil = *not* a Doing status. Something can be both Started,
  // and also postponed, at the same time. So add a separate
  // PageMeta.postponedTil field, to indicate sth has been postponed.

  def fromInt(value: Int): Option[PageDoingStatus] = Some(value match {
    case PageDoingStatus.Discussing.IntVal => Discussing
    case PageDoingStatus.Planned.IntVal => Planned
    case PageDoingStatus.Started.IntVal => Started
    case PageDoingStatus.Done.IntVal => Done
    case _ => return None
  })
}


sealed abstract class WriteWhat(protected val IntValue: Int) { def toInt = IntValue }
object WriteWhat {
  case object OriginalPost extends WriteWhat(1)
  case object ReplyToOriginalPost extends WriteWhat(2)
  case object Reply extends WriteWhat(3)
  case object ChatComment extends WriteWhat(4)

  def fromInt(value: Int): Option[WriteWhat] = Some(value match {
    case OriginalPost.IntValue => OriginalPost
    case ReplyToOriginalPost.IntValue => ReplyToOriginalPost
    case Reply.IntValue => Reply
    case ChatComment.IntValue => ChatComment
    case _ => return None
  })
}



sealed abstract class PinPageWhere { def toInt: Int }

object PinPageWhere {
  // Don't change the IntValue:s — they're stored in the database.

  case object InCategory extends PinPageWhere { val IntValue = 1; def toInt = IntValue }
  case object Globally extends PinPageWhere { val IntValue = 3; def toInt = IntValue }

  def fromInt(int: Int): Option[PinPageWhere] = Some(int match {
    case InCategory.IntValue => InCategory
    case Globally.IntValue => Globally
    case _ => return None
  })

  def fromString(string: String): Option[PinPageWhere] = Some(string match {
    case "InCategory" => InCategory
    case "Globally" => Globally
    case _ => return None
  })
}



case class PageQuery(  // also see PeopleQuery
  orderOffset: PageOrderOffset,
  pageFilter: PageFilter,
  includeAboutCategoryPages: Boolean)

/** How to sort pages, and where to start listing them, e.g. if fetching additional
  * pages after the user has scrolled down to the end of a page list.
  */
sealed abstract class PageOrderOffset

object PageOrderOffset {
  //case object Any extends PageOrderOffset
  case object ByPath extends PageOrderOffset
  case object ByPublTime extends PageOrderOffset
  case object ByPinOrderLoadOnlyPinned extends PageOrderOffset
  case class ByBumpTime(offset: Option[ju.Date]) extends PageOrderOffset
  case class ByCreatedAt(offset: Option[ju.Date]) extends PageOrderOffset
  case class ByLikesAndBumpTime(offset: Option[(Int, ju.Date)]) extends PageOrderOffset
  case class ByScoreAndBumpTime(offset: Option[Float], period: TopTopicsPeriod)
    extends PageOrderOffset
}

case class PageFilter(
  filterType: PageFilterType,
  includeDeleted: Boolean)

sealed abstract class PageFilterType
object PageFilterType {
  case object AllTopics extends PageFilterType
  case object WaitingTopics extends PageFilterType
  case object ForActivitySummaryEmail extends PageFilterType
}


case class PagePostId(pageId: PageId, postId: PostId)

case class PagePostNr(pageId: PageId, postNr: PostNr) {
  def toList: List[AnyRef] = List(pageId, postNr.asInstanceOf[AnyRef])
}

case class PagePostNrId(pageId: PageId, postNr: PostNr, postId: PostId) {
}

