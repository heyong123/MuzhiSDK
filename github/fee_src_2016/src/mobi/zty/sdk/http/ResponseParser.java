package mobi.zty.sdk.http;

public interface ResponseParser<T> {

    public T getResponse(String response);

}
