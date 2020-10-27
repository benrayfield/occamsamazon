# occamsamazon
Tools to simplify AWS, including a java.util.Map&lt;String,byte[]> wrapper of a S3 bucket with (TODO) low lag putAll(Map&lt;String,byte[]>) and there will be a getAll(Set&lt;String>) and it gives you download url for each String key. Includes all dependencies except java.

You still have to do some manual setup on aws website, such as copying S3Map.bucketPolicyForPublicRead() to the bucket policy string, but later I might automate that part too.

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
