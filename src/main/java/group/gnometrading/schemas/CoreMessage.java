package group.gnometrading.schemas;

/** A message of the core schema: orders, intents, execution reports and metrics. */
public abstract class CoreMessage extends SbeMessage {

    protected final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    protected final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
}
