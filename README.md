# noo
spring helper lib

用来帮助快速开发基于spring的应用程序。在mybatis/jpa之外提供另外一种简便的编程方式。

现在流行的ORM方式，会所创建大量的值对象，比如pojo/do/dto，然后也会自定义出一套查询的语言，对sql做各种转化，我觉得这让简单的事情复杂化了，这也是这个项目之所以叫noo的含义，没有仅仅传递值的值对象，全部通过统一的Json对象代替，对数据库的查询，我会辅助以一些简便的方法，但是还是通过sql语句来完成。我认为这样的方式，会让开发更加简化快速。


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
	public int createBooks() {
			
		JsonObject param = Req.params();
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

book.queryByNameParam("select sn,name,price from book where {author=#au} and {country=:vc}", param);

/*
#au   如果param中不存在au值，该条件将被替换为1=2
:vc   如果param中不存在vc值，该条件将被替换为1=1
*/

```



