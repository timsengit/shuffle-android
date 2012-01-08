package org.dodgybits.shuffle.shared;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

import java.util.List;

@ServiceName(value = "org.dodgybits.shuffle.server.service.GlobalService",
        locator="org.dodgybits.shuffle.server.locator.DaoServiceLocator")
public interface GlobalService extends RequestContext {

    Request<List<Integer>> emptyTrash();

    Request<List<Integer>> deleteEverything();

}