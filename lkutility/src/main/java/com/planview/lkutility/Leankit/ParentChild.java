package com.planview.lkutility.leankit;

public class ParentChild {
    public String boardName, parentId, childId;

    public ParentChild(String board, String parent, String child){
        boardName = board;
        parentId = parent;
        childId = child;
    }
}
