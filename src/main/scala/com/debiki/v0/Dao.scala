// vim: fdm=marker et ts=2 sw=2 tw=80 fo=tcqwn list

package com.debiki.v0

import com.debiki.v0.Prelude._
import com.google.{common => guava}
import java.{util => ju}
import net.liftweb.common.{Logger, Box, Empty, Full, Failure}

/** Debiki's Data Access Object service provider interface.
 */
abstract class DaoSpi {

  def create(where: PagePath, debate: Debate): Box[Debate]

  def close()

  // COULD use:  save(Guid(tenantId, pageGuid), xs)  instead?
  def save[T](tenantId: String, debateId: String, xs: List[T]): Box[List[T]]

  def load(tenantId: String, debateId: String): Box[Debate]

  def loadTemplates(perhapsTmpls: List[PagePath]): List[Debate]

  def checkPagePath(pathToCheck: PagePath): Box[PagePath]

  def checkAccess(pagePath: PagePath, userId: String, action: Action
                     ): Option[IntrsAllowed]

  def checkRepoVersion(): Box[String]

  /** Used as salt when hashing e.g. email and IP, before the hash
   *  is included in HTML. */
  def secretSalt(): String

}

/** Debiki's Data Access Object.
 *
 *  Delegates database requests to a DaoSpi implementation.
 */
abstract class Dao {

  def create(where: PagePath, debate: Debate): Box[Debate]

  def close()

  def save[T](tenantId: String, debateId: String, xs: List[T]): Box[List[T]]

  def load(tenantId: String, debateId: String): Box[Debate]

  /** Looks up guids for each possible template.
   *
   *  Each perhaps-template is represented by a PagePath.
   *  The guids found are returned, but PagePaths that point to
   *  non-existing templates are filtered out.
   */
  def loadTemplates(perhapsTmpls: List[PagePath]): List[Debate]

  def checkPagePath(pathToCheck: PagePath): Box[PagePath]

  def checkAccess(pagePath: PagePath, userId: String, action: Action
                     ): Option[IntrsAllowed]

  def checkRepoVersion(): Box[String]

  def secretSalt(): String
}

/** Caches pages in a ConcurrentMap.
 *
 *  Thread safe, if `impl' is thread safe.
 */
class CachingDao(impl: DaoSpi) extends Dao {

  private val _impl = impl
  private case class Key(tenantId: String, debateId: String)

  private val _cache: ju.concurrent.ConcurrentMap[Key, Debate] =
      new guava.collect.MapMaker().
          softValues().
          maximumSize(100*1000).
          //expireAfterWrite(10. TimeUnits.MINUTES).
          makeComputingMap(new guava.base.Function[Key, Debate] {
            def apply(k: Key): Debate = {
              _impl.load(k.tenantId, k.debateId) openOr null
            }
          })

  def create(where: PagePath, debate: Debate): Box[Debate] = {
    for (debateWithIds <- _impl.create(where, debate)) yield {
      val key = Key(where.tenantId, debateWithIds.guid)
      val duplicate = _cache.putIfAbsent(key, debateWithIds)
      errorIf(duplicate ne null, "Newly created page "+ safed(debate.guid) +
          " found in mem cache [debiki_error_38WcK905]")
      debateWithIds
    }
  }

  def close() = _impl.close

  def save[T](tenantId: String, debateId: String, xs: List[T]
                 ): Box[List[T]] = {
    for (xsWithIds <- _impl.save(tenantId, debateId, xs)) yield {
      val key = Key(tenantId, debateId)
      var replaced = false
      while (!replaced) {
        val oldPage = _cache.get(key)
        val newPage = oldPage ++ xsWithIds
        // newPage might == oldPage, if another thread just refreshed
        // the page from the database.
        replaced = _cache.replace(key, oldPage, newPage)
        System.out.println("muu")
      }
      xsWithIds  // TODO return newPage instead? Or possibly a pair.
    }
  }

  def load(tenantId: String, debateId: String): Box[Debate] = {
    try {
      Full(_cache.get(Key(tenantId, debateId)))
    } catch {
      case e: NullPointerException =>
        Empty
    }
  }

  def loadTemplates(perhapsTmpls: List[PagePath]): List[Debate] =
    _impl.loadTemplates(perhapsTmpls)

  def checkPagePath(pathToCheck: PagePath): Box[PagePath] =
    _impl.checkPagePath(pathToCheck)

  def checkAccess(pagePath: PagePath, userId: String, action: Action
                     ): Option[IntrsAllowed] =
    _impl.checkAccess(pagePath, userId, action)

  def checkRepoVersion(): Box[String] = _impl.checkRepoVersion()

  def secretSalt(): String = _impl.secretSalt()

}

/** Always accesses the database, whenever you ask it do do something.
 *
 *  Useful when constructing test suites that should access the database.
 */
class NonCachingDao(impl: DaoSpi) extends Dao {
  def create(where: PagePath, debate: Debate): Box[Debate] =
    impl.create(where, debate)

  def close() = impl.close

  def save[T](tenantId: String, debateId: String, xs: List[T]): Box[List[T]] =
    impl.save(tenantId, debateId, xs)

  def load(tenantId: String, debateId: String): Box[Debate] =
    impl.load(tenantId, debateId)

  def loadTemplates(perhapsTmpls: List[PagePath]): List[Debate] =
    impl.loadTemplates(perhapsTmpls)

  def checkPagePath(pathToCheck: PagePath): Box[PagePath] =
    impl.checkPagePath(pathToCheck)

  def checkAccess(pagePath: PagePath, userId: String, action: Action
                     ): Option[IntrsAllowed] =
    impl.checkAccess(pagePath, userId, action)

  def checkRepoVersion(): Box[String] = impl.checkRepoVersion()

  def secretSalt(): String = impl.secretSalt()
}

