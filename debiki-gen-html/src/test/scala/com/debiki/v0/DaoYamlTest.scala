// vim: ts=2 sw=2 et
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.debiki.v0

import org.junit._
import Assert._
import org.yaml.{snakeyaml => y}

object DaoYamlTest {

  object Paths {
    val MmaDebate =
        "target/test-classes/v0/compactDebate/_debate.yaml"
  }
}

@Test
class DaoYamlTest {
  import DaoYamlTest._

  @Test
  def testLoad() {
    val dao: Dao = new DaoYaml
    val debate = dao.getDebate(Paths.MmaDebate)
    assert(debate.id == "compactDebate")
    assert(debate.postsById.size == 11)
  }

}