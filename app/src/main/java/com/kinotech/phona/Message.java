package com.kinotech.phona;


public class Message {
    private String msg_type;
    private String _id;
    private String thread_id;
    private String address;
    private String person;
    private String date;
    private String date_sent;
    private String protocol;
    private String read;
    private String status;
    private String type;
    private String reply_path_present;
    private String subject;
    private String body;
    private String service_center;
    private String locked;
    private String error_code;
    private String seen;
    private String deletable;
    private String hidden;
    private String group_id;
    private String group_type;
    private String delivery_date;
    private String app_id;
    private String msg_id;
    private String callback_number;
    private String reserved;
    private String pri;
    private String teleservice_id;
    private String link_url;

    public Message() {

    }

    public Message(String msgtype,String id,String threadid,String address,String person,String date,String datesent,String protocol,String read,
                   String status,String type,String rpp,String subject,String body,String service_center,String locked,String error,String seen,String deletable,
                   String hiden,String groupid,String grouptype,String deliverydate,String appid,String msgid,String callback,String reserved,String pri,String teleserv,String link) {

        this.msg_type  = msgtype;
        this._id       = id;
        this.thread_id = threadid;
        this.address   = address;
        this.person    = person;
        this.date      = date;
        this.date_sent = datesent;
        this.protocol  = protocol;
        this.read      = read;
        this.status    = status;
        this.type      = type;
        this.reply_path_present = rpp;
        this.subject   = subject ;
        this.body      = body;
        this.service_center = service_center;
        this.locked    = locked;
        this.error_code = error;
        this.seen       = seen;
        this.deletable  = deletable;
        this.hidden     = hiden;
        this.group_id   = groupid;
        this.group_type = grouptype;
        this.delivery_date = deliverydate;
        this.app_id     = appid;
        this.msg_id     = msgid;
        this.callback_number = callback;
        this.reserved   = reserved;
        this.pri        = pri;
        this.teleservice_id = teleserv;
        this.link_url   = link;

    }

    /**
     * Sets a message type. Example: received/sent/spam/draft
     * @param msg_type
     */
    public void setMsgType(String msg_type){ this.msg_type = msg_type;	}
    /**
     * Returns a message type. Example: received/sent/spam/draft
     *
     */
    public String getMsgType(){return msg_type;}

    public void setId(String _id){ this._id = _id;	}
    public String getId(){return _id;}

    public void setThread_id(String thread_id){ this.thread_id = thread_id;	}
    public String getThread_id(){return thread_id;}

    public void setAddress(String address){ this.address = address;	}
    public String getAddress(){return address;}

    public void setPerson(String person){ this.person = person;	}
    public String getPerson(){return person;}

    public void setDate(String date){ this.date = date;	}
    public String getDate(){return date;}

    public void setDateSent(String date_sent){ this.date_sent = date_sent;	}
    public String getDateSent(){return date_sent;}

    public void setProtocol(String protocol){ this.protocol = protocol;	}
    public String getProtocol(){return protocol;}

    public void setRead(String read){ this.read = read;	}
    public String getRead(){return read;}

    public void setStatus(String status){ this.status = status;	}
    public String getStatus(){return status;}

    public void setType(String type){this.type = type;}
    public String getType(){return this.type;}

    public void setReplyPath(String reply_path_present){ this.reply_path_present = reply_path_present;	}
    public String getReplyPath(){return reply_path_present;}

    public void setSubject(String subject){this.subject = subject;}
    public String getSubject(){return this.subject;}

    public void setBody(String body){this.body = body;}
    public String getBody(){return this.body;}

    public void setServiceCenter(String service_center){this.service_center = service_center;}
    public String getServiceCenter(){return this.service_center;}

    public void setLocked(String locked){this.locked = locked;}
    public String getLocked(){return this.locked;}

    public void setErrorCode(String error_code){this.error_code = error_code;}
    public String getErrorCode(){return this.error_code;}

    public void setSeen(String seen){this.seen = seen;}
    public String getSeen(){return this.seen;}

    public void setDeletable(String deletable){this.deletable = deletable;}
    public String getDeletable(){return this.deletable;}

    public void setHidden(String hidden){this.hidden = hidden;}
    public String getHidden(){return this.hidden;}

    public void setGroupId(String group_id){this.group_id = group_id;}
    public String getGroupId(){return this.group_id;}

    public void setGroupType(String group_type){this.group_type = group_type;}
    public String getGroupType(){return this.group_type;}

    public void setDeliveryDate(String delivery_date){this.delivery_date = delivery_date;}
    public String getDeliveryDate(){return this.delivery_date;}

    public void setAppId(String app_id){this.app_id = app_id;}
    public String getAppId(){return this.app_id;}

    public void setMsgId(String msg_id){this.msg_id = msg_id;}
    public String getMsgId(){return this.msg_id;}

    public void setCallbackNumber(String callback_number){this.callback_number = callback_number;}
    public String getCallbackNumber(){return this.callback_number;}

    public void setReserved(String reserved){this.reserved = reserved;}
    public String getReserved(){return this.reserved;}

    public void setPri(String pri){this.pri = pri;}
    public String getPri(){return this.pri;}

    public void setTeleserviceId(String teleservice_id){this.teleservice_id = teleservice_id;}
    public String getTeleserviceId(){return this.teleservice_id;}

    public void setLinkUrl(String link_url){this.link_url = link_url;}
    public String getLinkUrl(){return this.link_url;}

}

