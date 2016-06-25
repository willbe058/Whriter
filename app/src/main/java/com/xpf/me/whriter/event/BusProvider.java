package com.xpf.me.whriter.event;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.xpf.me.whriter.model.WhriterFile;

/**
 * Created by pengfeixie on 16/6/4.
 */
public class BusProvider {

    private static final Bus instance = new Bus(ThreadEnforcer.MAIN);

    public static Bus getInstance() {
        return instance;
    }

    public static class FolderChangeEvent {

        private WhriterFile currentFolder;

        public FolderChangeEvent(WhriterFile file) {
            this.currentFolder = file;
        }

        public WhriterFile getCurrentFolder() {
            return currentFolder;
        }
    }

    public static class LongClickEvent {

        private boolean dismiss;

        public LongClickEvent(boolean dismiss) {
            this.dismiss = dismiss;
        }

        public boolean isDismiss() {
            return dismiss;
        }
    }


}
