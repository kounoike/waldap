package waldap.core.servlet

import javax.servlet._
import javax.servlet.http.HttpServletRequest

import org.scalatra.ScalatraBase
import org.slf4j.LoggerFactory
import waldap.core.util.{Database, Keys}
import waldap.core.model.Profile.profile.blockingApi._
import com.zaxxer.hikari._

class TransactionFilter extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  override def init(filterConfig: FilterConfig): Unit = {}

  override def destroy(): Unit = {}

  override def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain): Unit = {
    val servletPath = req.asInstanceOf[HttpServletRequest].getServletPath()
    if (servletPath.startsWith("/webjars") || servletPath.startsWith("/assets/")) {
      chain.doFilter(req, res)
    } else {
      Database() withTransaction { session =>
        // Register Scalatra error callback to rollback transaction
        ScalatraBase.onFailure { _ =>
          logger.debug("Rolled back transaction")
          session.conn.rollback()
        }(req.asInstanceOf[HttpServletRequest])

        logger.debug("begin transaction")
        req.setAttribute(Keys.Request.DBSession, session)
        chain.doFilter(req, res)
        logger.debug("end transaction")
      }
    }
  }
}
