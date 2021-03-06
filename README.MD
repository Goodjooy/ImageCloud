# Image Storage

## API接口

### **Web Security ```/```**  

* ```/sign-in```
  * 用户登录接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | POST | uid={*userEmailAddress*}&<br>paswd={*userPassword*}| uid->用户登录用邮箱;<br>paswd->用户登录密码| 登录<br>*进行用户认证*|所有人| ```Result<Boolean>``` |
* ```/sign-up```
  * 用户注册接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
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

* ```/user-find-password```
  * 测试用的用户找回密码页面
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | email={*userEmailAddress*} | email->用户邮箱 | 获得简单的用户找回密码界面 | USER | ```html``` |
* ```/find-password```
  * 发送用户找回密码验证码
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | email={*userEmailAddress*} | email->用户邮箱 | 将生成的验证码发送给用户邮箱 | 所有人 | ```Result<Boolean>``` |
       POST | email={*userEmailAddress*}&<br>code={*findPasswordVerifyCode*}&<br>paswd={*newPassword*} | email->用户邮箱；<br>code->验证码；<br>paswd->新密码 | 验证验证码是否未过期且相等，如果验证通过就修改密码<br>**前端记得让用户确认2次密码** | 所有人 | ```Result<Boolean>``` |

### **File System ```/```**

* ```/imgPreview/{filename:.+}```
  * 只供測試使用的圖片預覽接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |
      | GET**removed** | \[路徑參數]filename | filename->要預覽的圖片名稱 | 預覽圖片 | USER | ```html``` |
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
      | GET | path={*targetPath*}&<br>withHidden={*withHiddenItems*:**false**}&<br>sort={*sortDependentBy*:**off**}&<br>reverse={*reverseSort*:**false**} | path->要獲取的文件夾或者文件信息的路徑;<br>withHidden(boolean)->是否忽略隐藏文件;<br>sort->排序方式；<br>reverse->是否反转排序 | 獲取指定文件夾或文件信息<br>*路徑以```/root```開頭*<br>*可用的排序方法<br>{`name`, `createTime`,`modifyTime`}* | USER | ```Result<Item>``` |
      | POST | path={*fileUploadDirPath*}&<br>file={*fileToUpload*}&<br>hidden={*createHiddenFiles*:**false**} | path->文件上傳的位置（不包含文件名）;<br>file->要上傳的文件;<br>hidden(bool)->是否创建隐藏文件 | 將文件上傳到指定位置<br>*路徑以```/root```開頭*<br>*如果路徑不存在，將會自動創建文件夾* | USER | ```Result<List<Result<Boolean>>>```|
      | DELETE | path={*targetPathsToRemove*}&<br>paswd={*userPassword*}&<br>flat={*removeFlatly*:**true**}&<br>removeTargetDir={*removeTargetDir*:**false**} | path->要刪除的文件夾或文件**支持多参数，用`,`分割**;<br>paswd->用戶密碼;<br>flat(bool)->是否为浅层删除；<br>removeTargetDir(bool)->是否删除指定的文件夹 | 將指定位置文件或文件夾刪除<br>*路徑以```/root```開頭*<br>*浅层删除不会删除服务器文件，未来可能提供接口供恢复* | USER |```Result<List<Result<Boolean>>>```|

* ```/dir```
  * 創建文件夾接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | path={*pathToCreate*}&<br>hidden={*createHiddenDir*:**false**} | path->要被創建的目錄路徑;<br>hidden(bool)->创建的文件夹为隐藏文件夹 | 創建指定路徑上全部缺失文件夾<br>*路徑以```/root```開頭* | USER |```Result<Boolean>```|

### **User File Operate `/`**

* ```/rename```
  * 文件夹重命名接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | oldPath={*targetFileOrDirPath*}&<br>newName={*newNameOfTargetFileOrDir*} | oldPath->目標文件或文件夾路徑（包括文件或文件夾）;<br>newName->目標文件或文件夾的新名稱（不包含路徑） | 將指定位置的文件或者文件夾重命名<br>*路徑以```/root```開頭* | USER |```Result<String>```|
* ```/file-status```
  * 改变文件夹或者文件状态
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | path={*targetFilesToChangeStatus*} | path->要改变状态的文件夹或者文件**支持多参数，用`,`分割** | 改变给定文件夹或者文件隐藏状态<br>**不会影响子文件夹和文件的状态** | USER |```Result<List<Result<Boolean>>>```|
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
      | GET | null | null | 获得用户删除的文件树 | USER | ```Result<Map<String,Item>>``` |
* ```/move```
  * 移动指定文件
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | origins={*itemPathsToMove*}&<br>target={*targetDir*}&<br>sameNameOK={*generateIdCodeForSameNameItem*:**true**} | origins->要被移动的源文件/文件夹；<br>target->移动的目标文件；<br>sameNameOK->在移动目标出现同名文件夹/文件夹将生成区分码 | 将指定的文件/文件夹移动到目标位置<br>*源文件夹支持多文件* | USER | ```Result<List<Result<Boolean>>>``` |

* ```/share```
  * 分享指定的文件
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | targets={*itemPathsToShare*} | targets->要分享的文件/文件夹路径 | 分享指定的文件/文件夹，返回生成的分享码 | USER | ```Result<String>``` |
* ```/catch-share```
  * 分享指定的文件
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | shareCode={*targetShareCode*}&<br>targetPath={*itemPathToSaveItems*} |shareCode->分享码；<br> targetPath->要保存分享的文件/文件夹的文件夹路径 | 保存分享文件<br>*目标文件夹下出现同名将会添加区分码* | USER | ```Result<Boolean>``` |

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
      | GET | \[路径参数]size<br>\[路径参数]filename | size->要获取的头像大小；<br>filename->头像的实际文件名 | 获取用户头像信息<br>*头像大小只支持以下范围<br>{512，256，128，64，32，16}* | USER |```Result<UserImage>``` |
* ```/head```
  * 获取基本的用户信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | POST | file={*UserHeadImageToUpload*} | file->要上传的用户头像 | 上传用户头像<br>*如果图片不是正方形，将选取中央最大正方形作为头像* | USER |```Result<Boolean>``` |

* ```/verify```

  * 发送用户邮箱验证
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET |   null   | null | 发送邮箱验证到用户邮箱 | USER |```Result<Boolean>``` |
     | POST | code={*verifyCode*} | code->邮箱验证码 | 检查验证码，更新用户验证状态 | USER |```Result<Boolean>```|
* ```/rename```
  * 用户重命名
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: |:------: |
      | POST | newName={*newUserName*} | newName->用户新名字 | 更换用户用户名 | USER |```Result<String>```|
* ```/reset-paswd```
  * 用戶重置密碼
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 獲得簡單的更改密碼界面 | USER | ```html``` |
* ```/user-verify```
  * 测试用的用户邮箱验证码验证页面
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null | null | 获得简单的用户邮箱验证界面 | USER | ```html``` |

### **File Share `/sharePort`**

* ```/s/{shareCode:.+}```
  * 获取分享信息
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET | \[路径参数]shareCode | shareCode->目标分享码 | 获取对应分享的内容 | 所有人 | ```List<Item>``` |

### **Test Port  `/test`**

* ```/sign-in```
  * 用户登录接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :-----: | :-----: | :--: | :-----: | :------: |
      | GET | null | null | 获得简陋登录界面 | 所有人 | ```html``` |
* ```/sign-up```
  * 用户注册接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null | null | 获得简陋注冊界面 | 所有人 | ```html``` |
* ```/upload```
  * 只供測試使用的文件上傳接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 獲得簡單的文件上傳界面 | 所有人 | ```html``` |
* ```/headup```
  * 只供測試使用的用户头像上傳接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 获得简单的用户头像上传界面 | 所有人 | ```html``` |
* ```/headImagePreview```
  * 只供測試使用的用户头像预览接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 | 响应模型 |
      | :--: | :--: | :--: | :--: | :--: | :------: |  
      | GET | null |null| 获得简单的用户头像预览界面 | 所有人 | ```html``` |

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
  
  class ItemTime{
    LocalDateTime createTime;
    LocalDateTime modifyTime;
    LocalDateTime deleteTime;
  }
  
  class Item{
    String itemName;
    ItemType itemType;
    ItemTime time;
    List<Item> subItems;
    FileStorage file;
    boolean hidden;
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
    
     String formatTotalSize;
     String formatUsedSize;
  
     boolean verify;
  }
  ```

## 异常描述

* File System Exception
  | 异常名称 | 异常触发条件 |
  | :-----: | :---------: |
  | `EmptyFileException` | 上传文件为空 |
  | `FileFormatNotSupportException` | 上传文件的扩展名不支持 |
  | `ImageSizeNotSupport` | 头像的缩放尺寸不支持 |
  | `StorageException` | 保存文件异常 |
  | `StorageFileNotFoundException` | 保存在服务器的文件没找到 |
* Item Exception
  | 异常名称 | 异常触发条件 |
  | :-----: | :---------: |
  | `ItemExistException` | 新建文件夹目标文件夹已经存在,<br>恢复文件/文件夹时目录中有同名未删除的文件夹,<br>重命名文件/文件夹时同目录下有同名文件/文件夹 |
  | `ItemNotFoundException` | 指定路径不存在 |
  | `RootDeleteException` | 请求删除`/rrot`目录 |
  | `RootPathCanNotBeHiddenException` | 请求隐藏`/root`目录 |
  | `RootPathNotExistException` | 根目录`/root`不存在 |
  | `UnknownItemTypeException` | 未知的`item`类型 |

* User Exception
  | 异常名称 | 异常触发条件 |
  | :-----: | :---------: |
  | `BadUserInformationException` | 不符合要求的用户信息（包括密码长度，用户名长度） |
  | `EmailAddressNotSupportException` | 不支持的邮箱地址{邮箱格式错误，邮箱已经被使用} |
  | `NotAllowRequestException` | 用户无权访问目标内容 |
  | `NotEnoughTimeBetweenDifferentVerifyCodeGenerationException` | 发送验证码间隔时间过短(<60s>) |
  | `ResetPasswordFailureException` | 重置密码失败{旧密码错误} |
  | `SamePasswordException` | 新旧密码一致 |
  | `UserNotFoundException` | 没找到目标用户 |
  | `UserNotVerifiedException` | 用户未进行邮箱验证 |
  | `UserSpaceNotEnoughException` | 用户剩余存储空间不足 |
  | `UserVerifiedException` | 用户已经验证 |
  | `VerifyFailureException` | 用户验证失败 |
