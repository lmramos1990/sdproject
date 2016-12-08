package web;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import java.util.Map;

public class LoginInterceptor implements Interceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        System.out.println("[INTERCEPTOR] I JUST RAN");
        Map<String, Object> session = invocation.getInvocationContext().getSession();
        String isSession;

        if(session.get("username") != null) {
            isSession = Action.SUCCESS;
        } else {
            isSession = Action.LOGIN;
        }


        return isSession;
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}
