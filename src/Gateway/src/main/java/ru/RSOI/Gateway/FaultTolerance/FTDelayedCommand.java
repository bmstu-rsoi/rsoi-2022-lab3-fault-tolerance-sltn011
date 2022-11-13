package ru.RSOI.Gateway.FaultTolerance;

import java.util.UUID;

public class FTDelayedCommand {

    public enum Type
    {
        CarUncheck,

        RentCancel,
        RentFinish,

        PaymentCancel
    }

    public Type Command;
    public UUID DataUID;
    public String Username;

    public FTDelayedCommand(Type Command, UUID DataUID, String Username)
    {
        this.Command = Command;
        this.DataUID = DataUID;
        this.Username = Username;
    }
}
