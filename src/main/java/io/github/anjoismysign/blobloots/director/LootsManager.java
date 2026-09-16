package io.github.anjoismysign.blobloots.director;

import io.github.anjoismysign.bloblib.manager.GenericManager;
import io.github.anjoismysign.blobloots.BlobLoots;

public class LootsManager extends GenericManager<BlobLoots, LootsManagerDirector> {

    public LootsManager(LootsManagerDirector managerDirector) {
        super(managerDirector);
    }
}