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

package com.debiki.dao.rdb

import com.debiki.core._
import com.debiki.core.Prelude._
import java.sql.ResultSet
import scala.collection.mutable
import Rdb._
import RdbUtil._


/** Creates, updates, deletes and loads settings for e.g. the whole website, a section
  * of the site (e.g. a blog or a forum), a category, single pages.
  */
trait SettingsSiteDaoMixin extends SiteTransaction {
  self: RdbSiteTransaction =>


  override def loadSiteSettings(): Option[EditedSettings] = {
    val query = s"""
      select *
      from settings3
      where site_id = ? and category_id is null and page_id is null
      """
    runQueryFindOneOrNone(query, List(siteId.asAnyRef), readSettingsFromResultSet)
  }


  override def upsertSiteSettings(settings: SettingsToSave) {
    // Later: use Postgres' built-in upsert (when have upgraded to Postgres 9.5)
    if (loadSiteSettings().isDefined) {
      updateSiteSettings(settings)
    }
    else {
      insertSiteSettings(settings)
    }
  }


  private def insertSiteSettings(editedSettings2: SettingsToSave) {
    val statement = s"""
      insert into settings3 (
        site_id,
        category_id,
        page_id,
        user_must_be_auth,
        user_must_be_approved,
        expire_idle_after_mins,
        invite_only,
        allow_signup,
        allow_local_signup,
        allow_guest_login,
        enable_google_login,
        enable_facebook_login,
        enable_twitter_login,
        enable_github_login,
        enable_gitlab_login,
        enable_linkedin_login,
        enable_vk_login,
        enable_instagram_login,
        require_verified_email,
        email_domain_blacklist,
        email_domain_whitelist,
        may_compose_before_signup,
        may_login_before_email_verified,
        double_type_email_address,
        double_type_password,
        beg_for_email_address,
        enable_sso,
        sso_url,
        sso_not_approved_url,
        sso_login_required_logout_url,
        forum_main_view,
        forum_topics_sort_buttons,
        forum_category_links,
        forum_topics_layout,
        forum_categories_layout,
        show_categories,
        show_topic_filter,
        show_topic_types,
        select_topic_type,
        show_author_how,
        watchbar_starts_open,
        discussion_layout,
        disc_post_nesting,
        disc_post_sort_order,
        progress_layout,
        orig_post_reply_btn_title,
        orig_post_votes,
        num_first_posts_to_review,
        num_first_posts_to_approve,
        num_first_posts_to_allow,
        enable_stop_forum_spam,
        enable_akismet,
        akismet_api_key,
        send_email_to_akismet,
        favicon_url,
        head_styles_html,
        head_scripts_html,
        end_of_body_html,
        header_html,
        footer_html,
        horizontal_comments,
        social_links_html,
        logo_url_or_html,
        org_domain,
        org_full_name,
        org_short_name,
        terms_of_use_url,
        privacy_url,
        rules_url,
        contact_email_addr,
        contact_url,
        contrib_agreement,
        content_license,
        language_code,
        google_analytics_id,
        enable_forum,
        enable_api,
        enable_tags,
        enable_chat,
        enable_direct_messages,
        enable_similar_topics,
        show_sub_communities,
        experimental,
        feature_flags,
        allow_embedding_from,
        embedded_comments_category_id,
        html_tag_css_classes)
      values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
          ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
          ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
          ?)
      """
    val values = List(
      siteId.asAnyRef,
      NullInt,
      NullVarchar,
      editedSettings2.userMustBeAuthenticated.getOrElse(None).orNullBoolean,
      editedSettings2.userMustBeApproved.getOrElse(None).orNullBoolean,
      editedSettings2.expireIdleAfterMins.getOrElse(None).orNullInt,
      editedSettings2.inviteOnly.getOrElse(None).orNullBoolean,
      editedSettings2.allowSignup.getOrElse(None).orNullBoolean,
      editedSettings2.allowLocalSignup.getOrElse(None).orNullBoolean,
      editedSettings2.allowGuestLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableGoogleLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableFacebookLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableTwitterLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableGitHubLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableGitLabLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableLinkedInLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableVkLogin.getOrElse(None).orNullBoolean,
      editedSettings2.enableInstagramLogin.getOrElse(None).orNullBoolean,
      editedSettings2.requireVerifiedEmail.getOrElse(None).orNullBoolean,
      editedSettings2.emailDomainBlacklist.getOrElse(None).trimOrNullVarchar,
      editedSettings2.emailDomainWhitelist.getOrElse(None).trimOrNullVarchar,
      editedSettings2.mayComposeBeforeSignup.getOrElse(None).orNullBoolean,
      editedSettings2.mayPostBeforeEmailVerified.getOrElse(None).orNullBoolean,
      editedSettings2.doubleTypeEmailAddress.getOrElse(None).orNullBoolean,
      editedSettings2.doubleTypePassword.getOrElse(None).orNullBoolean,
      editedSettings2.begForEmailAddress.getOrElse(None).orNullBoolean,
      editedSettings2.enableSso.getOrElse(None).orNullBoolean,
      editedSettings2.ssoUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.ssoNotApprovedUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.ssoLoginRequiredLogoutUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.forumMainView.getOrElse(None).trimOrNullVarchar,
      editedSettings2.forumTopicsSortButtons.getOrElse(None).trimOrNullVarchar,
      editedSettings2.forumCategoryLinks.getOrElse(None).trimOrNullVarchar,
      editedSettings2.forumTopicsLayout.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.forumCategoriesLayout.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.showCategories.getOrElse(None).orNullBoolean,
      editedSettings2.showTopicFilterButton.getOrElse(None).orNullBoolean,
      editedSettings2.showTopicTypes.getOrElse(None).orNullBoolean,
      editedSettings2.selectTopicType.getOrElse(None).orNullBoolean,
      editedSettings2.showAuthorHow.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.watchbarStartsOpen.getOrElse(None).orNullBoolean,
      editedSettings2.discussionLayout.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.discPostNesting.getOrElse(None).orNullInt,
      editedSettings2.discPostSortOrder.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.progressLayout.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.origPostReplyBtnTitle.getOrElse(None).orNullVarchar,
      editedSettings2.origPostVotes.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.numFirstPostsToReview.getOrElse(None).orNullInt,
      editedSettings2.numFirstPostsToApprove.getOrElse(None).orNullInt,
      editedSettings2.numFirstPostsToAllow.getOrElse(None).orNullInt,
      editedSettings2.enableStopForumSpam.getOrElse(None).orNullBoolean,
      editedSettings2.enableAkismet.getOrElse(None).orNullBoolean,
      editedSettings2.akismetApiKey.getOrElse(None).orNullVarchar,
      editedSettings2.sendEmailToAkismet.getOrElse(None).orNullBoolean,
      editedSettings2.faviconUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.headStylesHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.headScriptsHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.endOfBodyHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.headerHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.footerHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.horizontalComments.getOrElse(None).orNullBoolean,
      editedSettings2.socialLinksHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.logoUrlOrHtml.getOrElse(None).trimOrNullVarchar,
      editedSettings2.orgDomain.getOrElse(None).trimOrNullVarchar,
      editedSettings2.orgFullName.getOrElse(None).trimOrNullVarchar,
      editedSettings2.orgShortName.getOrElse(None).trimOrNullVarchar,
      editedSettings2.termsOfUseUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.privacyUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.rulesUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.contactEmailAddr.getOrElse(None).trimOrNullVarchar,
      editedSettings2.contactUrl.getOrElse(None).trimOrNullVarchar,
      editedSettings2.contribAgreement.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.contentLicense.getOrElse(None).map(_.toInt).orNullInt,
      editedSettings2.languageCode.getOrElse(None).trimOrNullVarchar,
      editedSettings2.googleUniversalAnalyticsTrackingId.getOrElse(None).trimOrNullVarchar,
      editedSettings2.enableForum.getOrElse(None).orNullBoolean,
      editedSettings2.enableApi.getOrElse(None).orNullBoolean,
      editedSettings2.enableTags.getOrElse(None).orNullBoolean,
      editedSettings2.enableChat.getOrElse(None).orNullBoolean,
      editedSettings2.enableDirectMessages.getOrElse(None).orNullBoolean,
      editedSettings2.enableSimilarTopics.getOrElse(None).orNullBoolean,
      editedSettings2.showSubCommunities.getOrElse(None).orNullBoolean,
      editedSettings2.showExperimental.getOrElse(None).orNullBoolean,
      editedSettings2.featureFlags.getOrElse(None).trimOrNullVarchar,
      editedSettings2.allowEmbeddingFrom.getOrElse(None).trimOrNullVarchar,
      editedSettings2.embeddedCommentsCategoryId.getOrElse(None).orNullInt,
      editedSettings2.htmlTagCssClasses.getOrElse(None).trimOrNullVarchar)

    runUpdate(statement, values)
  }


  private def updateSiteSettings(editedSettings2: SettingsToSave) {
    val statement = mutable.StringBuilder.newBuilder.append("update settings3 set ")
    val values = mutable.ArrayBuffer[AnyRef]()
    var somethingToDo = false
    var nothingOrComma = ""

    def maybeSet(column: String, anyValue: Option[AnyRef]) {
      anyValue foreach { value =>
        somethingToDo = true
        statement.append(s"$nothingOrComma$column = ?")
        nothingOrComma = ", "
        values += value
      }
    }

    val s = editedSettings2
    maybeSet("user_must_be_auth", s.userMustBeAuthenticated.map(_.orNullBoolean))
    maybeSet("user_must_be_approved", s.userMustBeApproved.map(_.orNullBoolean))
    maybeSet("expire_idle_after_mins", s.expireIdleAfterMins.map(_.orNullInt))
    maybeSet("invite_only", s.inviteOnly.map(_.orNullBoolean))
    maybeSet("allow_signup", s.allowSignup.map(_.orNullBoolean))
    maybeSet("allow_local_signup", s.allowLocalSignup.map(_.orNullBoolean))
    maybeSet("allow_guest_login", s.allowGuestLogin.map(_.orNullBoolean))
    maybeSet("enable_google_login", s.enableGoogleLogin.map(_.orNullBoolean))
    maybeSet("enable_facebook_login", s.enableFacebookLogin.map(_.orNullBoolean))
    maybeSet("enable_twitter_login", s.enableTwitterLogin.map(_.orNullBoolean))
    maybeSet("enable_github_login", s.enableGitHubLogin.map(_.orNullBoolean))
    maybeSet("enable_gitlab_login", s.enableGitLabLogin.map(_.orNullBoolean))
    maybeSet("enable_linkedin_login", s.enableLinkedInLogin.map(_.orNullBoolean))
    maybeSet("enable_vk_login", s.enableVkLogin.map(_.orNullBoolean))
    maybeSet("enable_instagram_login", s.enableInstagramLogin.map(_.orNullBoolean))
    maybeSet("require_verified_email", s.requireVerifiedEmail.map(_.orNullBoolean))
    maybeSet("email_domain_blacklist", s.emailDomainBlacklist.map(_.trimOrNullVarchar))
    maybeSet("email_domain_whitelist", s.emailDomainWhitelist.map(_.trimOrNullVarchar))
    maybeSet("may_compose_before_signup", s.mayComposeBeforeSignup.map(_.orNullBoolean))
    maybeSet("may_login_before_email_verified", s.mayPostBeforeEmailVerified.map(_.orNullBoolean))
    maybeSet("double_type_email_address", s.doubleTypeEmailAddress.map(_.orNullBoolean))
    maybeSet("double_type_password", s.doubleTypePassword.map(_.orNullBoolean))
    maybeSet("beg_for_email_address", s.begForEmailAddress.map(_.orNullBoolean))
    maybeSet("enable_sso", s.enableSso.map(_.orNullBoolean))
    maybeSet("sso_url", s.ssoUrl.map(_.trimOrNullVarchar))
    maybeSet("sso_not_approved_url", s.ssoNotApprovedUrl.map(_.trimOrNullVarchar))
    maybeSet("sso_login_required_logout_url", s.ssoLoginRequiredLogoutUrl.map(_.trimOrNullVarchar))
    maybeSet("forum_main_view", s.forumMainView.map(_.trimOrNullVarchar))
    maybeSet("forum_topics_sort_buttons", s.forumTopicsSortButtons.map(_.trimOrNullVarchar))
    maybeSet("forum_category_links", s.forumCategoryLinks.map(_.trimOrNullVarchar))
    maybeSet("forum_topics_layout", s.forumTopicsLayout.map(_.map(_.toInt).orNullInt))
    maybeSet("forum_categories_layout", s.forumCategoriesLayout.map(_.map(_.toInt).orNullInt))
    maybeSet("show_categories", s.showCategories.map(_.orNullBoolean))
    maybeSet("show_topic_filter", s.showTopicFilterButton.map(_.orNullBoolean))
    maybeSet("show_topic_types", s.showTopicTypes.map(_.orNullBoolean))
    maybeSet("select_topic_type", s.selectTopicType.map(_.orNullBoolean))
    maybeSet("show_author_how", s.showAuthorHow.map(_.map(_.toInt).orNullInt))
    maybeSet("watchbar_starts_open", s.watchbarStartsOpen.map(_.orNullBoolean))
    maybeSet("discussion_layout", s.discussionLayout.map(_.map(_.toInt).orNullInt))
    maybeSet("disc_post_nesting", s.discPostNesting.map(_.orNullInt))
    maybeSet("disc_post_sort_order", s.discPostSortOrder.map(_.map(_.toInt).orNullInt))
    maybeSet("progress_layout", s.progressLayout.map(_.map(_.toInt).orNullInt))
    maybeSet("orig_post_reply_btn_title", s.origPostReplyBtnTitle.map(_.trimOrNullVarchar))
    maybeSet("orig_post_votes", s.origPostVotes.map(_.map(_.toInt).orNullInt))
    maybeSet("num_first_posts_to_review", s.numFirstPostsToReview.map(_.orNullInt))
    maybeSet("num_first_posts_to_approve", s.numFirstPostsToApprove.map(_.orNullInt))
    maybeSet("num_first_posts_to_allow", s.numFirstPostsToAllow.map(_.orNullInt))
    maybeSet("enable_stop_forum_spam", s.enableStopForumSpam.map(_.orNullBoolean))
    maybeSet("enable_akismet", s.enableAkismet.map(_.orNullBoolean))
    maybeSet("akismet_api_key", s.akismetApiKey.map(_.trimOrNullVarchar))
    maybeSet("send_email_to_akismet", s.sendEmailToAkismet.map(_.orNullBoolean))
    maybeSet("favicon_url", s.faviconUrl.map(_.trimOrNullVarchar))
    maybeSet("head_styles_html", s.headStylesHtml.map(_.trimOrNullVarchar))
    maybeSet("head_scripts_html", s.headScriptsHtml.map(_.trimOrNullVarchar))
    maybeSet("end_of_body_html", s.endOfBodyHtml.map(_.trimOrNullVarchar))
    maybeSet("header_html", s.headerHtml.map(_.trimOrNullVarchar))
    maybeSet("footer_html", s.footerHtml.map(_.trimOrNullVarchar))
    maybeSet("horizontal_comments", s.horizontalComments.map(_.orNullBoolean))
    maybeSet("social_links_html", s.socialLinksHtml.map(_.trimOrNullVarchar))
    maybeSet("logo_url_or_html", s.logoUrlOrHtml.map(_.trimOrNullVarchar))
    maybeSet("org_domain", s.orgDomain.map(_.trimOrNullVarchar))
    maybeSet("org_full_name", s.orgFullName.map(_.trimOrNullVarchar))
    maybeSet("org_short_name", s.orgShortName.map(_.trimOrNullVarchar))
    maybeSet("terms_of_use_url", s.termsOfUseUrl.map(_.trimOrNullVarchar))
    maybeSet("privacy_url", s.privacyUrl.map(_.trimOrNullVarchar))
    maybeSet("rules_url", s.rulesUrl.map(_.trimOrNullVarchar))
    maybeSet("contact_email_addr", s.contactEmailAddr.map(_.trimOrNullVarchar))
    maybeSet("contact_url", s.contactUrl.map(_.trimOrNullVarchar))
    maybeSet("contrib_agreement", s.contribAgreement.map(_.map(_.toInt).orNullInt))
    maybeSet("content_license", s.contentLicense.map(_.map(_.toInt).orNullInt))
    maybeSet("language_code", s.languageCode.map(_.trimOrNullVarchar))
    maybeSet("google_analytics_id", s.googleUniversalAnalyticsTrackingId.map(_.trimOrNullVarchar))
    maybeSet("enable_forum", s.enableForum.map(_.orNullBoolean))
    maybeSet("enable_api", s.enableApi.map(_.orNullBoolean))
    maybeSet("enable_tags", s.enableTags.map(_.orNullBoolean))
    maybeSet("enable_chat", s.enableChat.map(_.orNullBoolean))
    maybeSet("enable_direct_messages", s.enableDirectMessages.map(_.orNullBoolean))
    maybeSet("enable_similar_topics", s.enableSimilarTopics.map(_.orNullBoolean))
    maybeSet("show_sub_communities", s.showSubCommunities.map(_.orNullBoolean))
    maybeSet("experimental", s.showExperimental.map(_.orNullBoolean))
    maybeSet("feature_flags", s.featureFlags.map(_.trimOrNullVarchar))
    maybeSet("allow_embedding_from", s.allowEmbeddingFrom.map(_.trimOrNullVarchar))
    maybeSet("embedded_comments_category_id", s.embeddedCommentsCategoryId.map(_.orNullInt))
    maybeSet("html_tag_css_classes", s.htmlTagCssClasses.map(_.trimOrNullVarchar))
    maybeSet("num_flags_to_hide_post", s.numFlagsToHidePost.map(_.orNullInt))
    maybeSet("cooldown_minutes_after_flagged_hidden",
                s.cooldownMinutesAfterFlaggedHidden.map(_.orNullInt))
    maybeSet("num_flags_to_block_new_user", s.numFlagsToBlockNewUser.map(_.orNullInt))
    maybeSet("num_flaggers_to_block_new_user", s.numFlaggersToBlockNewUser.map(_.orNullInt))
    maybeSet("notify_mods_if_user_blocked", s.notifyModsIfUserBlocked.map(_.orNullBoolean))
    maybeSet("regular_member_flag_weight", s.regularMemberFlagWeight.map(_.orNullFloat))
    maybeSet("core_member_flag_weight", s.coreMemberFlagWeight.map(_.orNullFloat))

    statement.append(" where site_id = ? and category_id is null and page_id is null")
    values.append(siteId.asAnyRef)

    if (somethingToDo) {
      runUpdateExactlyOneRow(statement.toString(), values.toList)
    }
  }


  private def readSettingsFromResultSet(rs: ResultSet): EditedSettings = {
    EditedSettings(
      userMustBeAuthenticated = getOptBoolean(rs, "user_must_be_auth"),
      userMustBeApproved = getOptBoolean(rs, "user_must_be_approved"),
      expireIdleAfterMins = getOptInt(rs, "expire_idle_after_mins"),
      inviteOnly = getOptBoolean(rs, "invite_only"),
      allowSignup = getOptBoolean(rs, "allow_signup"),
      allowLocalSignup = getOptBoolean(rs, "allow_local_signup"),
      allowGuestLogin = getOptBoolean(rs, "allow_guest_login"),
      enableGoogleLogin = getOptBoolean(rs, "enable_google_login"),
      enableFacebookLogin = getOptBoolean(rs, "enable_facebook_login"),
      enableTwitterLogin = getOptBoolean(rs, "enable_twitter_login"),
      enableGitHubLogin = getOptBoolean(rs, "enable_github_login"),
      enableGitLabLogin = getOptBoolean(rs, "enable_gitlab_login"),
      enableLinkedInLogin = getOptBoolean(rs, "enable_linkedin_login"),
      enableVkLogin = getOptBoolean(rs, "enable_vk_login"),
      enableInstagramLogin = getOptBoolean(rs, "enable_instagram_login"),
      requireVerifiedEmail = getOptBoolean(rs, "require_verified_email"),
      emailDomainBlacklist = getOptString(rs, "email_domain_blacklist"),
      emailDomainWhitelist = getOptString(rs, "email_domain_whitelist"),
      mayComposeBeforeSignup = getOptBoolean(rs, "may_compose_before_signup"),
      mayPostBeforeEmailVerified = getOptBoolean(rs, "may_login_before_email_verified"),
      doubleTypeEmailAddress = getOptBoolean(rs, "double_type_email_address"),
      doubleTypePassword = getOptBoolean(rs, "double_type_password"),
      minPasswordLength = None,
      begForEmailAddress = getOptBoolean(rs, "beg_for_email_address"),
      enableSso = getOptBoolean(rs, "enable_sso"),
      ssoUrl = getOptString(rs, "sso_url"),
      ssoNotApprovedUrl = getOptString(rs, "sso_not_approved_url"),
      ssoLoginRequiredLogoutUrl = getOptString(rs, "sso_login_required_logout_url"),
      forumMainView = getOptString(rs, "forum_main_view"),
      forumTopicsSortButtons = getOptString(rs, "forum_topics_sort_buttons"),
      forumCategoryLinks = getOptString(rs, "forum_category_links"),
      forumTopicsLayout = getOptInt(rs, "forum_topics_layout").flatMap(TopicListLayout.fromInt),
      forumCategoriesLayout = getOptInt(rs, "forum_categories_layout").flatMap(CategoriesLayout.fromInt),
      showCategories = getOptBool(rs, "show_categories"),
      showTopicFilterButton = getOptBool(rs, "show_topic_filter"),
      showTopicTypes = getOptBool(rs, "show_topic_types"),
      selectTopicType = getOptBool(rs, "select_topic_type"),
      showAuthorHow = getOptInt(rs, "show_author_how").flatMap(ShowAuthorHow.fromInt),
      watchbarStartsOpen = getOptBool(rs, "watchbar_starts_open"),
      discussionLayout = getOptInt(rs, "discussion_layout").flatMap(DiscussionLayout.fromInt),
      discPostNesting = getOptInt(rs, "disc_post_nesting"),
      discPostSortOrder = getOptInt(rs, "disc_post_sort_order").flatMap(PostSortOrder.fromInt),
      progressLayout = getOptInt(rs, "progress_layout").flatMap(ProgressLayout.fromInt),
      origPostReplyBtnTitle = getOptString(rs, "orig_post_reply_btn_title"),
      origPostVotes = getOptInt(rs, "orig_post_votes").flatMap(OrigPostVotes.fromInt),
      numFirstPostsToReview = getOptInt(rs, "num_first_posts_to_review"),
      numFirstPostsToApprove = getOptInt(rs, "num_first_posts_to_approve"),
      numFirstPostsToAllow = getOptInt(rs, "num_first_posts_to_allow"),
      enableStopForumSpam = getOptBool(rs, "enable_stop_forum_spam"),
      enableAkismet = getOptBool(rs, "enable_akismet"),
      akismetApiKey = getOptString(rs, "akismet_api_key"),
      sendEmailToAkismet = getOptBool(rs, "send_email_to_akismet"),
      faviconUrl = getOptString(rs, "favicon_url"),
      headStylesHtml = getOptString(rs, "head_styles_html"),
      headScriptsHtml = getOptString(rs, "head_scripts_html"),
      endOfBodyHtml = getOptString(rs, "end_of_body_html"),
      headerHtml = getOptString(rs, "header_html"),
      footerHtml = getOptString(rs, "footer_html"),
      horizontalComments = getOptBool(rs, "horizontal_comments"),
      socialLinksHtml = getOptString(rs, "social_links_html"),
      logoUrlOrHtml = getOptString(rs, "logo_url_or_html"),
      orgDomain = getOptString(rs, "org_domain"),
      orgFullName = getOptString(rs, "org_full_name"),
      orgShortName = getOptString(rs, "org_short_name"),
      termsOfUseUrl = getOptString(rs, "terms_of_use_url"),
      privacyUrl = getOptString(rs, "privacy_url"),
      rulesUrl = getOptString(rs, "rules_url"),
      contactEmailAddr = getOptString(rs, "contact_email_addr"),
      contactUrl = getOptString(rs, "contact_url"),
      contribAgreement = ContribAgreement.fromInt(rs.getInt("contrib_agreement")), // 0 -> None, ok
      contentLicense = ContentLicense.fromInt(rs.getInt("content_license")), // 0 -> None, ok
      languageCode = getOptString(rs, "language_code"),
      googleUniversalAnalyticsTrackingId = getOptString(rs, "google_analytics_id"),
      enableForum = getOptBool(rs, "enable_forum"),
      enableApi = getOptBool(rs, "enable_api"),
      enableTags = getOptBool(rs, "enable_tags"),
      enableChat = getOptBool(rs, "enable_chat"),
      enableDirectMessages = getOptBool(rs, "enable_direct_messages"),
      enableSimilarTopics = getOptBool(rs, "enable_similar_topics"),
      showSubCommunities = getOptBool(rs, "show_sub_communities"),
      showExperimental = getOptBool(rs, "experimental"),
      featureFlags = getOptString(rs, "feature_flags"),
      allowEmbeddingFrom = getOptString(rs, "allow_embedding_from"),
      embeddedCommentsCategoryId = getOptInt(rs, "embedded_comments_category_id"),
      htmlTagCssClasses = getOptString(rs, "html_tag_css_classes"),
      numFlagsToHidePost = getOptInt(rs, "num_flags_to_hide_post"),
      cooldownMinutesAfterFlaggedHidden = getOptInt(rs, "cooldown_minutes_after_flagged_hidden"),
      numFlagsToBlockNewUser = getOptInt(rs, "num_flags_to_block_new_user"),
      numFlaggersToBlockNewUser = getOptInt(rs, "num_flaggers_to_block_new_user"),
      notifyModsIfUserBlocked = getOptBool(rs, "notify_mods_if_user_blocked"),
      regularMemberFlagWeight = getOptFloat(rs, "regular_member_flag_weight"),
      coreMemberFlagWeight = getOptFloat(rs, "core_member_flag_weight"))
  }

}
