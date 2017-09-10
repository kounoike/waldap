package waldap.core.servlet

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import waldap.core.service.SystemSettingsService.SystemSettings

class AdminAuthenticationFilter extends Filter {
  override def init(filterConfig: FilterConfig) = {}

  override def destroy() = {}

  override def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) ={
    implicit val request = servletRequest.asInstanceOf[HttpServletRequest]
     val response = servletResponse.asInstanceOf[HttpServletResponse]
  }
}
