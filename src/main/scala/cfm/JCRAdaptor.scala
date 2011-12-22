package cfm

import org.apache.jackrabbit.core.{TransientRepository}
import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory
import org.apache.jackrabbit.core.security.principal.AdminPrincipal
import java.util.HashMap
import org.apache.jackrabbit.jcr2dav.Jcr2davRepositoryFactory
import javax.jcr.{SimpleCredentials, Session, Credentials, Repository => JCRRepo}

trait JCRAdaptor {
  val repo: JCRRepo
  val s: Session
}

class TransientRepoAdaptor(creds: Credentials) extends JCRAdaptor {
  val repo = new TransientRepository()
  implicit lazy val s = repo.login(creds)
}

object TransientRepoAdaptor {
  def apply() = new TransientRepoAdaptor(new SimpleCredentials("admin", "admin".toCharArray))
}

class RMIRepoAdaptor(val host: String,
                     val port: Int,
                     val appPath: String,
                     val workSpace: String,
                     val userName: String,
                     val password: String) extends JCRAdaptor {
  def this() = {
    this ("localhost", 1234, "crx", "crx.default", "admin", "admin")
  }

  private[this] def getRepo = {
    repo
  }

  private def buildUrl(host: String, port: Int, workspace: String) = "//" + host + ":" + port + "/" + workspace

  lazy val repo = new ClientRepositoryFactory().getRepository(buildUrl(host, port, appPath))
  val s = repo.login(new SimpleCredentials(userName, password.toCharArray), workSpace)
}

object RMIRepoAdaptor {
  def apply() = new RMIRepoAdaptor()
}

class DavRepoAdaptor(val url: String, val workSpace: String, val userName: String, val password: String) extends JCRAdaptor {

  def this() = this ("http://localhost:4502/crx/server", "crx.default", "admin", "admin")

  lazy val repo = getRepo
  lazy val s = repo.login(new SimpleCredentials(userName, password.toCharArray))

  private[this] def getRepo = {
    val props = new HashMap[String, Object]()
    props.put("org.apache.jackrabbit.spi2davex.uri", url)
    new Jcr2davRepositoryFactory().getRepository(props)
  }
}

object DavRepoAdaptor {
  def apply = new DavRepoAdaptor()
}
