package org.innoswp.innotable.controller.forwarder;

import org.innoswp.innotable.model.Pair;
import org.innoswp.innotable.model.file.MetaFile;
import org.innoswp.innotable.model.user.Group;
import org.innoswp.innotable.model.user.User;

import java.util.List;

public interface FileForwarder {

    void pushEventForUser(User user, List<MetaFile> userMetaFiles);

    void pushEventsForGroup(Group group, List<MetaFile> groupMetaFiles);

    void pushAllEvents(List<Pair<Group, MetaFile>> metaFiles);
}
