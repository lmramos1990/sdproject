package web.interceptor;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import web.action.LoginAction;
import web.action.RegisterAction;

import java.util.Map;

public class MyInterceptor implements Interceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        System.out.println("[INTERCEPTOR] I JUST RAN");
        Map<String, Object> session = invocation.getInvocationContext().getSession();

        Object action = invocation.getAction();
        if(action instanceof LoginAction && session.containsKey("loggedin")) return Action.SUCCESS;
        if(action instanceof RegisterAction && session.containsKey("loggedin")) return "home";
        if(!(action instanceof LoginAction) && !(action instanceof RegisterAction) && !session.containsKey("loggedin")) return Action.LOGIN;

        return invocation.invoke();
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}
