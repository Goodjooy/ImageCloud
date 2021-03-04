package com.jacky.imagecloud.exceptionHandle;

import com.jacky.imagecloud.data.LoggerHandle;
import com.jacky.imagecloud.data.Result;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
public class ErrorHandle extends AbstractHandlerExceptionResolver {

    LoggerHandle logger=LoggerHandle.newLogger(ErrorHandle.class);
    /**
     * Actually resolve the given exception that got thrown during handler execution,
     * returning a {@link ModelAndView} that represents a specific error page if appropriate.
     * <p>May be overridden in subclasses, in order to apply specific exception checks.
     * Note that this template method will be invoked <i>after</i> checking whether this
     * resolved applies ("mappedHandlers" etc), so an implementation may simply proceed
     * with its actual exception handling.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the executed handler, or {@code null} if none chosen at the time
     *                 of the exception (for example, if multipart resolution failed)
     * @param ex       the exception that got thrown during handler execution
     * @return a corresponding {@code ModelAndView} to forward to,
     * or {@code null} for default processing in the resolution chain
     */
    @Override
    protected ModelAndView doResolveException(@NotNull HttpServletRequest request,
                                              @NotNull HttpServletResponse response,
                                              Object handler,
                                              @NotNull Exception ex) {
        String Url=request.getRequestURI();
        logger.error(ex,"`Exception Catch` | RequestURL<%s> | Code Local<%s>",request.getRequestURI(),
                Objects.requireNonNull(handler).toString());

        var result= Result.exceptionCatchResult(ex,request);
        var r=new ModelAndView(new MappingJackson2JsonView());
        r.addObject("data",result.data);
        r.addObject("err",result.err);
        r.addObject("message",result.message);

        return r;
    }
}
