package com.github.storytime.model.jaxb.history.response.error;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public Response createResponse() {
        return new Response();
    }

    public Response.Data createResponseData() {
        return new Response.Data();
    }

    public Response.Data.Error createResponseDataError() {
        return new Response.Data.Error();
    }

}
