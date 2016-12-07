package web;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import java.util.Map;

public class LoginInterceptor implements Interceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = invocation.getInvocationContext().getSession();

        if(session.get("username") != null && session.get("loggedin") != null) {
            System.out.println(invocation.invoke());
            return invocation.invoke();
        } else {
            return Action.SUCCESS;
        }
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}
