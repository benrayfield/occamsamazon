/** Ben F Rayfield offers this software opensource MIT license */
package mutable.occamsamazon;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import immutable.util.Text;
import mutable.util.ByteStreams;
import mutable.util.Files;
import mutable.util.FunctionMap;

/** a map wrapping an amazon S3 bucket which is remote storage of shortString->byte[] mappings.
FIXME You still have to create the bucket and adjust aws s3 permissions on aws website (TODO do that part here too).
*/
public class S3Map extends HashMap<String,byte[]> implements Map<String,byte[]>{
	
	public static final String amazonSetupToDoBeforeRunningThisMap = "create aws acct and put in a money card (todo trying the prepaid card, but still need to prevent it from charging too much cuz they will still say its owed if exceed that). create access key and secret key, which are both small strings and are found in the dropdown menu on aws console website under your name then security credentials. turn on accelerated transfers to pay more for it being faster (just uploads or it appeared to speed up a download).";
	
	public String bucketPolicyForPublicRead(){
		return "{\r\n"
			+"    \"Version\": \"2012-10-17\",\r\n"
			+"    \"Statement\": [\r\n"
			+"        {\r\n"
			+"            \"Sid\": \"PublicReadGetObject\",\r\n"
			+"            \"Action\": \"s3:GetObject\",\r\n"
			+"            \"Effect\": \"Allow\",\r\n"
			+"            \"Resource\": \"arn:aws:s3:::"+region+"/*\",\r\n"
			+"            \"Principal\": \"*\"\r\n"
			+"        }\r\n"
			+"    ]\r\n"
			+"}";
	}
	
	protected AmazonS3 s3;
	
	protected String region;
	
	protected String bucket;
	
	protected final boolean allowWrite, allowRead, cacheImmutable;
	
	public S3Map(String awsAccessKey, String awsSecretKey, String region, String bucket, boolean cacheImmutable){
		this(awsAccessKey, awsSecretKey, region, bucket, true, true, cacheImmutable);
	}
	
	public S3Map(String awsAccessKey, String awsSecretKey, String region, String bucket, boolean allowWrite, boolean allowRead, boolean cacheImmutable){
		this(
			AmazonS3ClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
				.withRegion(region)
				.build(),
			bucket,
			allowWrite,
			allowRead,
			cacheImmutable
		);
		this.region = region;
	}
	
	public S3Map(AmazonS3 s3, String bucket, boolean cacheImmutable){
		this(s3, bucket, true, true, cacheImmutable);
	}
	
	public S3Map(AmazonS3 s3, String bucket, boolean allowWrite, boolean allowRead, boolean cacheImmutable){
		this.s3 = s3;
		this.region = s3.getRegionName();
		this.bucket = bucket;
		this.allowWrite = allowWrite;
		this.allowRead = allowRead;
		this.cacheImmutable = cacheImmutable;
	}
	
	public byte[] get(Object name){
		String n = (String)name;
		if(!allowRead) throw new UnsupportedOperationException("!allowRead");
		return ByteStreams.bytes(s3.getObject(new GetObjectRequest(bucket,n)).getObjectContent());
	}
	
	/** same param as get(String). returns url to download it after put. */
	public String url(String name){
		return "https://"+bucket+".s3-"+region+".amazonaws.com/"+name; //FIXME escape name? what kind of escape?
	}
	
	public byte[] put(String name, byte[] value){
		put(name, new ByteArrayInputStream(value), value.length);
		return value; //as if put it twice, cuz dont want to GET cuz that wastes network.
	}
	
	public void put(String name, InputStream value, long byteLen){
		if(!allowWrite) throw new UnsupportedOperationException("!allowWrite");
		//s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));
		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(byteLen);
		meta.setContentDisposition("inline");
		meta.setContentType("*"); //browser figures this out for most types, and if you dont say this, amazon defaults to application/octet-stream which doesnt inline
		if(cacheImmutable){
			meta.setCacheControl("immutable"); //cache forever if you like. Will never cause any delayed updates.
			//for detecting changes in content so know not need to download again. https://en.wikipedia.org/wiki/HTTP_ETag
			//TODO? meta.setHeader("ETag", sha3_256 of content? dont want to scan it twice. caller likely already knows it this is used in simpleblobtable for example. amazon creates an etag);
		}
		s3.putObject(new PutObjectRequest(bucket, name, value, meta));
	}
	
	/** TODO all in 1 network call for speed.
	TODO getAll similarly.
	*/
	public void putAll(Map<? extends String, ? extends byte[]> m){
		throw new RuntimeException("TODO all in 1 network call");
	}
	
	public static void main(String... args){
		String dir = "yourDir";
		System.out.println("dir: "+dir);
		boolean immutable = false;
		Map<String,byte[]> m = new S3Map(Files.readStr(dir+"aws_access_key_id"), Files.readStr(dir+"aws_secret_access_key"), "us-west-2", "YOUR_BUCKET_NAME", immutable);
		S3Map M = (S3Map)m;
		m.put("hello", Text.stringToBytes("world"));
		m.put("hallo", Text.stringToBytes("ween"));
		String world = Text.bytesToString(m.get("hello"));
		System.out.println("world = "+world);
		
		Function<String,byte[]> sToB = Text::stringToBytes;
		Function<byte[],String> bToS = Text::bytesToString;
		Map<String,String> sm = new FunctionMap(sToB, bToS, m);
		
		System.out.println("get hallo = "+sm.get("hallo"));
		sm.put("abc", "def");
		System.out.println("get abc = "+sm.get("abc"));
		m.put("test.jpg", Files.read(new File("J:\\q35x\\pic\\programming\\compare python java c++ unixShell assembly C latex html.jpg")));
		String url = M.url("test.jpg");
		System.out.println("url = "+url);
	}

}
