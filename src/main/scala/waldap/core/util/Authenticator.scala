package waldap.core.util

import waldap.core.controller.ControllerBase

trait AdminAuthenticator { self: ControllerBase =>
  protected def adminOnly(action: => Any): Any = { authenticate(action) }
  protected def adminOnly[T](action: T => Any): T => Any = (form: T) => { authenticate(action(form)) }

  private def authenticate(action: => Any) = {
    {
      context.loginAccount match {
        case Some(x) if x.isAdmin => action
        case _ => UnauthorizedAdmin()
      }
    }
  }
}
