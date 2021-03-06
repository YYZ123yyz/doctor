package entity.liveroom;

import java.io.Serializable;

public class PreLiveInfo implements Serializable {
    private String attrName;
    private String broadcastCoverImgUrl;
    private String broadcastTitle;
    private String broadcastTypeName;
    private String broadcastUserLogoUrl;
    private String broadcastUserName;
    private String chatRoomCode;
    private String className;
    private String detailsCode;
    private String detailsId;
    private String extendBroadcastFollowNum;
    private String extendBroadcastPriceShow;
    private String pullUrl;
    private String userCode;
    private Integer flagLikes;//点赞标识.0:未点赞;1:已点赞;
    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getDetailsId() {
        return detailsId;
    }

    public void setDetailsId(String detailsId) {
        this.detailsId = detailsId;
    }

    public String getDetailsCode() {
        return detailsCode;
    }

    public void setDetailsCode(String detailsCode) {
        this.detailsCode = detailsCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getBroadcastUserLogoUrl() {
        return broadcastUserLogoUrl;
    }

    public void setBroadcastUserLogoUrl(String broadcastUserLogoUrl) {
        this.broadcastUserLogoUrl = broadcastUserLogoUrl;
    }

    public String getBroadcastUserName() {
        return broadcastUserName;
    }

    public void setBroadcastUserName(String broadcastUserName) {
        this.broadcastUserName = broadcastUserName;
    }

    public String getBroadcastTypeName() {
        return broadcastTypeName;
    }

    public void setBroadcastTypeName(String broadcastTypeName) {
        this.broadcastTypeName = broadcastTypeName;
    }

    public String getBroadcastTitle() {
        return broadcastTitle;
    }

    public void setBroadcastTitle(String broadcastTitle) {
        this.broadcastTitle = broadcastTitle;
    }

    public String getBroadcastCoverImgUrl() {
        return broadcastCoverImgUrl;
    }

    public void setBroadcastCoverImgUrl(String broadcastCoverImgUrl) {
        this.broadcastCoverImgUrl = broadcastCoverImgUrl;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPullUrl() {
        return pullUrl;
    }

    public void setPullUrl(String pullUrl) {
        this.pullUrl = pullUrl;
    }

    public String getChatRoomCode() {
        return chatRoomCode;
    }

    public void setChatRoomCode(String chatRoomCode) {
        this.chatRoomCode = chatRoomCode;
    }

    public String getExtendBroadcastFollowNum() {
        return extendBroadcastFollowNum;
    }

    public void setExtendBroadcastFollowNum(String extendBroadcastFollowNum) {
        this.extendBroadcastFollowNum = extendBroadcastFollowNum;
    }

    public String getExtendBroadcastPriceShow() {
        return extendBroadcastPriceShow;
    }

    public void setExtendBroadcastPriceShow(String extendBroadcastPriceShow) {
        this.extendBroadcastPriceShow = extendBroadcastPriceShow;
    }

    public Integer getFlagLikes() {
        return flagLikes;
    }

    public void setFlagLikes(Integer flagLikes) {
        this.flagLikes = flagLikes;
    }
}
