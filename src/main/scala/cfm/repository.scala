package cfm

import java.util.{Iterator => JIter}
import javax.jcr.version.{Version, VersionIterator}
import javax.jcr.nodetype.{NodeType, NodeTypeIterator}
import javax.jcr.observation.{Event, EventIterator, EventListener, EventListenerIterator, EventJournal}
import javax.jcr.security.{AccessControlPolicyIterator, AccessControlPolicy}
import javax.jcr._
import query.{Query, Row, RowIterator}
import org.apache.jackrabbit.commons.JcrUtils
import cfm.Repo.RichQuery

abstract class Repo[T](val url: String) {
  var repo: Repository
  var user: String
  var password: String
}

object Repo {

  trait RangeIter {
    def iter: RangeIterator
    def position = iter.getPosition
    def nodeSize = iter.getSize
    def skip(num: Long) = iter.skip(num)
    def remove() = iter.remove()
    def hasNext = iter.hasNext
  }

  // iterator implicits
  implicit def NodeItr2Iterator[A](i: NodeIterator): Iterator[Node] = new Iterator[Node] with RangeIter {
    def iter = i;
    def next(): Node = iter.next.asInstanceOf[Node]
  }

  implicit def PropItr2Iterator[A](i: PropertyIterator): Iterator[Property] = new Iterator[Property] with RangeIter {
    def iter = i;
    def next(): Property = iter.nextProperty.asInstanceOf[Property]
  }

  implicit def VersionItr2Iterator[A](i: VersionIterator): Iterator[Version] = new Iterator[Version] with RangeIter {
    def iter = i;
    def next(): Version = iter.nextVersion.asInstanceOf[Version]
  }

  implicit def NodeTypeItr2Iterator[A](i: NodeTypeIterator): Iterator[NodeType] = new Iterator[NodeType] with RangeIter {
    def iter = i;
    def next(): NodeType = iter.nextNodeType.asInstanceOf[NodeType]
  }

  implicit def RowItr2Iterator[A](i: RowIterator): Iterator[Row] = new Iterator[Row] with RangeIter {
    def iter = i;
    def next(): Row = i.nextRow.asInstanceOf[Row]
  }

  implicit def EventItr2Iterator[A](i: EventJournal): Iterator[Event] = new Iterator[Event] with RangeIter {
    def iter = i;
    def next(): Event = iter.nextEvent.asInstanceOf[Event];
    def skipTo(date: Long) = i.skipTo(date)
  }

  implicit def EventJournalItr2Iterator[A](i: EventIterator): Iterator[Event] = new Iterator[Event] with RangeIter {
    def iter = i;

    def next(): Event = iter.nextEvent.asInstanceOf[Event]
  }

  implicit def EventListenerItr2Iterator[A](i: EventListenerIterator): Iterator[EventListener] = new Iterator[EventListener] with RangeIter {
    def iter = i;
    def next(): EventListener = iter.nextEventListener.asInstanceOf[EventListener]
  }

  implicit def AccessControlPolicyItr2Iterator[A](i: AccessControlPolicyIterator): Iterator[AccessControlPolicy] = new Iterator[AccessControlPolicy] with RangeIter {
    def iter = i;
    def next(): AccessControlPolicy = iter.nextAccessControlPolicy.asInstanceOf[AccessControlPolicy]
  }

  implicit def node2RichNode(n: Node) = new RichNode(n)

  implicit def sess2RichSession(s: Session) = new RichSession(s)

  implicit def query2RichQuery(q: Query) = new RichQuery(q)

  def ls(path: String)(implicit s: Session) {
    val t = s.getRootNode.getNode(path)
    for (p <- t.getNodes) {
      println(p.getName)
    }
  }

  def collect(node: Node, a: Node => Boolean): List[Node] = {
    def c(ns: List[Node], nd: Node): List[Node] = {
      nd.getNodes.toList match {
        case Nil => if(a(nd)) nd :: ns else ns
        case f: List[Node] => if(a(nd)) nd :: f.flatMap(m => c(ns, m)) else f.flatMap(m => c(ns, m))}
    }
    c(List(), node)
  }
  def printItem(n: Item): Unit = println(JcrUtils.toString(n))

  def printItem(n: Item, f:Item => String):String = {printItem(n); f(n)}

  def collect(node: Node): List[Node] = collect(node, f => true)

  class RichSession(s: Session){
    def root() = s.getRootNode
    def xpath(query: String) = s.getWorkspace.getQueryManager.createQuery(query, Query.XPATH)
    def sql(query: String) = s.getWorkspace.getQueryManager.createQuery(query, Query.SQL)
  }

  class RichNode(n:Node){
    def xpath(query: String) = n.getSession.getWorkspace.getQueryManager.createQuery(query, Query.XPATH)
    def sql(query: String) = n.getSession.getWorkspace.getQueryManager.createQuery(query, Query.SQL)
    def collect() = Repo.collect(n, f => true)
    def collect(a: Node => Boolean) = Repo.collect(n, a)
    def hasSNS(): Boolean = n.getNodes.exists(f => f.getIndex > 1)
    def nodes() = n.getNodes
    def props() = n.getProperties

    def \() = n.getNodes

    def \(c: String) = n.getNode(c)

    def <@(p: String) = n.getProperty(p)
  }

  class RichQuery(q: Query){

    def limit(l: Long) ={
      q.setLimit(l)
      q
    }
    def offset(l: Long) ={
      q.setOffset(l)
      q
    }

    def bind(s: String, v: Value) = {
      q.bindValue(s, v)
      q
    }

  }


}