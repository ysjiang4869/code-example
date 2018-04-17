package org.jys.foundation;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by ysjiang on 2017/12/5.
 *
 */
public class JxTestClass {
    private UUID id;

    private String code;

    private short objectstate;

    private short category;

    private UUID accountowner;

    private short creditlevel;

    private double balance;

    private double creditlimit;

    private int balancetoken;

    private int creditlimittoken;

    private Instant expiredate;

    private String bankname;

    private String bankaccount;

    private short securitylevel;

    private short rowstate;

    private int crtuser;

    private Instant crtdate;

    private int upduser;

    private Instant upddate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public short getObjectstate() {
        return objectstate;
    }

    public void setObjectstate(short objectstate) {
        this.objectstate = objectstate;
    }

    public short getCategory() {
        return category;
    }

    public void setCategory(short category) {
        this.category = category;
    }

    public UUID getAccountowner() {
        return accountowner;
    }

    public void setAccountowner(UUID accountowner) {
        this.accountowner = accountowner;
    }

    public short getCreditlevel() {
        return creditlevel;
    }

    public void setCreditlevel(short creditlevel) {
        this.creditlevel = creditlevel;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getCreditlimit() {
        return creditlimit;
    }

    public void setCreditlimit(double creditlimit) {
        this.creditlimit = creditlimit;
    }

    public int getBalancetoken() {
        return balancetoken;
    }

    public void setBalancetoken(int balancetoken) {
        this.balancetoken = balancetoken;
    }

    public int getCreditlimittoken() {
        return creditlimittoken;
    }

    public void setCreditlimittoken(int creditlimittoken) {
        this.creditlimittoken = creditlimittoken;
    }

    public Instant getExpiredate() {
        return expiredate;
    }

    public void setExpiredate(Instant expiredate) {
        this.expiredate = expiredate;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getBankaccount() {
        return bankaccount;
    }

    public void setBankaccount(String bankaccount) {
        this.bankaccount = bankaccount;
    }

    public short getSecuritylevel() {
        return securitylevel;
    }

    public void setSecuritylevel(short securitylevel) {
        this.securitylevel = securitylevel;
    }

    public short getRowstate() {
        return rowstate;
    }

    public void setRowstate(short rowstate) {
        this.rowstate = rowstate;
    }

    public int getCrtuser() {
        return crtuser;
    }

    public void setCrtuser(int crtuser) {
        this.crtuser = crtuser;
    }

    public Instant getCrtdate() {
        return crtdate;
    }

    public void setCrtdate(Instant crtdate) {
        this.crtdate = crtdate;
    }

    public int getUpduser() {
        return upduser;
    }

    public void setUpduser(int upduser) {
        this.upduser = upduser;
    }

    public Instant getUpddate() {
        return upddate;
    }

    public void setUpddate(Instant upddate) {
        this.upddate = upddate;
    }
}
