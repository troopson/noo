# noo
spring helper lib

用来帮助快速开发基于spring的应用程序。在mybatis/jpa之外提供另外一种选择。

#### 引入noo
**Maven**

```xml
<dependency>
    <groupId>com.github.troopson</groupId>
	<artifactId>noo</artifactId>
	<version>1.0.52</version>
	<exclusions>
	    <exclusion>
		<artifactId>*</artifactId>
		<groupId>*</groupId>
	    </exclusion>
	</exclusions>
</dependency>

```

*依赖fastjson和guava，一般都加了，为了避免引入不同的版本，这里可以exclude掉*

**Gradle**
```groovy

    compile group: 'com.github.troopson', name: 'noo', version: '1.0.8', transitive: false

```


#### 代码示例

**Dao**

```java

@Repository
public class BookDao extends TDao {

/*
   BookDao对应数据库中的Book表，TDao中的方法通过类名获取表名，如果数据库对表名区分大小写，那么类名也需要保持一样。
   常用方法通过extends直接继承，复杂的操作可以添加自定义方法
*/

}

```

**Service**

```java

@Service
public class BookService {

	@Autowired
	private BookDao book;
	
	//没有VO、POJO、DO、DTO类，全部统一成为JsonObject和JsonArray
	
	public  JsonObject getBook(String id) {
		return book.getById(id);
	}
	
	public  int createBook(JsonObject b) {
		return book.insertRow(b);
	}
	
	@HystrixCommand(fallbackMethod="doqueryHystrix")
	public JsonArray doquery(Map<String,String> j) {  
	    return book.findAll(); 
	}
	
	public JsonArray doqueryHystrix(Map<String,String> j) {
		return null;
	}
	
}

```

**Controller**

```java

@Controller 
public class BookController {

    @Autowired
    private BookService service;
    
    
	@RequestMapping("/book")
	public int createBooks(JsonObject param) {
			 
		....
		int i = service.createBook(param);
		....
		return i;
    }
 
 }

```



#### SQL语法糖


```java

JsonObject param = new JsonObject();
param.put("au","Toe");
param.put("vc","Australia");
param.put("type" Arrays.asList("type-a","type-b"));

book.queryByNameParam("select sn,name,price from book where {author=#au} and {country=:vc} and {type in :type}", param);

/*
#au   如果param中不存在au值，该条件将被替换为1=2
:vc   如果param中不存在vc值，该条件将被替换为1=1
*/

```




