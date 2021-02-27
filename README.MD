# Image Storage

## API接口

### **Web Security ```/```**  

* ```/sign-in```
  * 用户登录接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET | null | null | 获得简陋登录界面 | 所有人| ```html``` |
      | POST | uid={*userEmailAddress*}&<br>paswd={*userPassword*}| uid->用户登录用邮箱;<br>paswd->用户登录密码| 登录<br>*进行用户认证*|所有人| ```Result<Boolean>``` |
* ```/sign-up```
  * 用户注册接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null | null | 获得简陋注冊界面 | 所有人| ```html``` |
      | POST | uid={*userEmailAddress*}&<br>paswd={*userPassword*}&<br>name={*userName*}| uid->用户登录用邮箱（*唯一*）;<br>paswd->用户登录密码;<br>name->用戶名| 注冊為新用戶<br>*如果郵箱已經被注冊將注冊失敗*|所有人|```Result<Boolean>``` |
* ```/sign-out```
  * 用戶登出
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制| 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null | null | 登出 | USER |```Result<Boolean>``` |

* ```/reset-paswd```
  * 用戶重置密碼
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 獲得簡單的更改密碼界面 | USER | ```html``` |
      | POST | old={*userOldPassword*}&<br>new={*userNewPassword*}|old->原密碼;<br>new->新密碼 | 將原密碼更換為新密碼<br>**如果新舊密碼一致或者原密碼錯誤將無法更改**| USER |```Result<Boolean>``` |
* ```/check-email```
  * 檢查郵箱是否已經被使用
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      |POST | email={*emailAddress*}|email->要檢查的email地址 | 檢查email是否已經被使用以及是否为合法email | 所有人|```Result<Boolean>``` |
* ```/session-status```
  * 检查当前的登录状态
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET  | null | null | 检查当前的登录状态 | 所有人 |```Result<Boolean>```|

### **File System ```/```**

* ```/upload```
  * 只供測試使用的文件上傳接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 獲得簡單的文件上傳界面 | USER | ```html``` |
* ```/imgPreview/{filename:.+}```
  * 只供測試使用的圖片預覽接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |
      | GET | \[路徑參數]filename | filename->要預覽的圖片名稱 | 預覽圖片 | USER | ```html``` |
* ```/storage/{filename:.+}```
  * 原圖獲取接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |
      | GET | \[路徑參數]filename | filename->要獲取的圖片名稱 | 獲取原圖 | USER |```ResponseEntity<Resource>```|
* ```/thumbnail/{filename:.+}```
  * 縮略圖獲取接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | GET | \[路徑參數]filename | filename->要獲取的圖片名稱 | 獲取縮略圖 | USER |```ResponseEntity<Resource>```|

### **User File ```/```**

* ```/file```
  * 用戶文件信息處理
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | GET | path={*targetPath*}&<br>withHidden={*ignoreHiddenItems*:**false**} | path->要獲取的文件夾或者文件信息的路徑;<br>withHidden(boolean)->是否忽略隐藏文件 | 獲取指定文件夾或文件信息<br>*路徑以```/root```開頭* | USER | ```Result<Item>``` |
      | POST | path={*fileUploadDirPath*}&<br>file={*fileToUpload*}&<br>hidden={*createHiddenFiles*:**false**} | path->文件上傳的位置（不包含文件名）;<br>file->要上傳的文件;<br>hidden(bool)->是否创建隐藏文件 | 將文件上傳到指定位置<br>*路徑以```/root```開頭*<br>*如果路徑不存在，將會自動創建文件夾* | USER | ```Result<Boolean>```|
      | DELETE | path={*targetPathToRemove*}&<br>paswd={*userPassword*}&<br>flat={*removeFlatly*:**true**}&<br>removeTargetDir={*removeTargetDir*:**false**} | path->要刪除的文件夾或文件;<br>paswd->用戶密碼;<br>flat(bool)->是否为浅层删除；<br>removeTargetDir(bool)->是否删除指定的文件夹 | 將指定位置文件或文件夾刪除<br>*路徑以```/root```開頭*<br>*浅层删除不会删除服务器文件，未来可能提供接口供恢复* | USER |```Result<Boolean>```|

* ```/dir```
  * 創建文件夾接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | path={*pathToCreate*}&<br>hidden={*createHiddenDir*:**false**} | path->要被創建的目錄路徑;<br>hidden(bool)->创建的文件夹为隐藏文件夹 | 創建指定路徑上全部缺失文件夾<br>*路徑以```/root```開頭* | USER |```Result<Boolean>```|
* ```/rename```
  * 創建文件夾接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | oldPath={*targetFileOrDirPath*}&<br>newName={*newNameOfTargetFileOrDir*} | oldPath->目標文件或文件夾路徑（包括文件或文件夾）;<br>newName->目標文件或文件夾的新名稱（不包含路徑） | 將指定位置的文件或者文件夾重命名<br>*路徑以```/root```開頭* | USER |```Result<Boolean>```|
* ```/file-status```
  * 改变文件夹或者文件状态
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | path={*targetFileTochangeStatus*} | path->要改变状态的文件夹或者文件 | 改变给定文件夹或者文件隐藏状态<br>**不会影响子文件夹和文件的状态** | USER |```Result<Boolean>```|
* ```/restore-file```
  * 恢复浅层删除的文件或者文件夹
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | path={*targetFileTochangeStatus*} | path->要恢复的文件夹或者文件 | 试图恢复浅删除的文件，未被删除无操作<br>**不会恢复子文件夹和文件** | USER |```Result<Boolean>```|
* ```/remove-trees```
  * 获得用户删除的文件树
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | GET | null | null | 获得用户删除的文件树 | USER | ```Result<Set<Item>>``` |

### **User Information ```/user```**

* ```/base```
  * 获取基本的用户信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET |   null   | null | 获取基本的用户信息 | USER |```Result<User>``` |
* ```/information```
  * 获取用户扩展信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET |   null   | null | 获取用户信息附加信息 | USER |```Result<UserInformation>``` |
* ```/image```
  * 获取用户头像信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET |   null   | null | 获取用户头像信息 | USER |```Result<UserImage>``` |
* ```/head{size:\d+}/{filename:.+}```
  * 获取用户头像信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET | \[路径参数]size<br>\[路径参数]filename | size->要获取的头像大小；<br>filename->头像的实际文件名 | 获取用户头像信息<br>*头像大小只支持以下范围{512，256，128，64，32，16}* | USER |```Result<UserImage>``` |

---

## 响应等效模型

* ```Result``` 模型
  
  ```java
  class Result<DATA>{
    DATA data;
    boolean err;
    String message;
  }
  ```

* ```Item```模型

  ```java
  enum ItemType {
        DIR,
        FILE
  }

  class FileStorage {
    String fileURL;
    String thumbnailURL;
  }

  class Item{
    int id;
    String itemName;
    ItemType itemType;
    Set<Item> subItems;
    FileStorage file;
    boolean hidden;
    int parentID;
  }
  ```

* ```User``` 模型
  
  ```java
    class User {
      String emailAddress;
      String name;
    }
  ```

* ```UserImage```模型

  ```java
    class UserImage {
      boolean setHeaded;

      String fileX512URL;
      String fileX256URL;
      String fileX128URL;
      String fileX64URL;
      String fileX32URL;
      String fileX16URL;
    }
  ```

* ```UserInformation``` 模型

  ```java
  class UserInformation {
     long totalSize;
     long usedSize;
  }
  ```
