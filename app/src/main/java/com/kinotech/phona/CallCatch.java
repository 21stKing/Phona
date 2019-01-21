package com.kinotech.phona;


public class CallCatch {

    private String call_id;
    private String phone_number;
    private String caller_name;
    private String call_type;
    private String call_date;
    private String call_duration;

    public CallCatch(String call_id, String phone, String caller, String type, String date, String duration)
    {
        this.call_id       = call_id;
        this.phone_number  = phone;
        this.caller_name   = caller;
        this.call_type     = type;
        this.call_date     = date;
        this.call_duration = duration;
    }

    public CallCatch(){

    }

    protected void setCallId(String callid){ this.call_id = callid;}
    protected void setPhoneNumber(String phone){ this.phone_number = phone;}
    protected void setCallerName(String caller){this.caller_name = caller;}
    protected void setCallType(String type){this.call_type = type;}
    protected void setCallDate(String date){this.call_date = date;}
    protected void setCallDuration(String duration){this.call_duration = duration;	}

    protected String getCallId(){return this.call_id;}
    protected String getPhoneNumber(){return this.phone_number;}
    protected String getCallerName(){return this.caller_name;}
    protected String getCallType(){return this.call_type;}
    protected String getCallDate(){return this.call_date;}
    protected String getCallDuration(){return this.call_duration;	}

}

