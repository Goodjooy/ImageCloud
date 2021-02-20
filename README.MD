# Image Storage

## API接口

### **Web Security**  

* ```/sign-in```
  * 用户登录接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null | null | 获得简陋登录界面 | 所有人|
      | POST | uid={*userEmailAddress*}&<br>paswd={*userPassword*}| uid->用户登录用邮箱;<br>paswd->用户登录密码| 登录<br>*进行用户认证*|所有人|
* ```/sign-up```
  * 用户注册接口
  * 参数列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null | null | 获得简陋注冊界面 | 所有人|
      | POST | uid={*userEmailAddress*}&<br>paswd={*userPassword*}&<br>name={*userName*}| uid->用户登录用邮箱（*唯一*）;<br>paswd->用户登录密码;<br>name->用戶名| 注冊為新用戶<br>*如果郵箱已經被注冊將注冊失敗*|所有人|
* ```/sign-out```
  * 用戶登出
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制|
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null | null | 登出 | USER |

* ```/reset-paswd```
  * 用戶重置密碼
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null |null| 獲得簡單的更改密碼界面 | USER |
      | POST | old={*userOldPassword*}&<br>new={*userNewPassword*}|old->原密碼;<br>new->新密碼 | 將原密碼更換為新密碼<br>**如果新舊密碼一致或者原密碼錯誤將無法更改**| USER |
* ```/check-email```
  * 檢查郵箱是否已經被使用
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      |POST | email={*emailAdress*}|email->要檢查的email地址 | 檢查email是否已經被使用 | 所有人|

### **File System**

* ```/upload```
  * 只供測試使用的文件上傳接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null |null| 獲得簡單的文件上傳界面 | USER |
* ```/imgPreview/{filename:.+}```
  * 只供測試使用的圖片預覽接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | \[路徑參數]filename | filename->要預覽的圖片名稱 | 預覽圖片 | USER |
* ```/storage/{filename:.+}```
  * 原圖獲取接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | \[路徑參數]filename | filename->要獲取的圖片名稱 | 獲取原圖 | USER |
* ```/thumbnail/{filename:.+}```
  * 縮略圖獲取接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 |功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | \[路徑參數]filename | filename->要獲取的圖片名稱 | 獲取縮略圖 | USER |

### **User File**

* ```/walk```
  * 獲取完整的用戶信息
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | null | null | 獲取完整的用戶信息 | USER |
* ```/file```
  * 用戶文件信息處理
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | GET | path={*tragetPath*} | path->要獲取的文件夾或者文件信息的路徑 | 獲取指定文件夾或文件信息<br>*路徑以```/root```開頭* | USER |
      | POST | path={*fileUploadDirPath*}&<br>file={*fileToUpload*} | path->文件上傳的位置（不包含文件名）;<br>file->要上傳的文件 | 將文件上傳到指定位置<br>*路徑以```/root```開頭*<br>*如果路徑不存在，將會自動創建文件夾* | USER |
      | DELETE | path={*tragetPathToRemove*}&<br>paswd={*userPassword*} | path->要刪除的文件夾或文件;<br>paswd->用戶密碼 | 將指定位置文件或文件夾刪除<br>*路徑以```/root```開頭*<br>*如果是文件夾，文件夾内子文件夾和文件將被全部刪除*<br>*如果是文件夾并且```path```以“```/```”結尾，將只清空目標文件夾，否則目標文件夾將一并刪除* | USER |

* ```/dir```
  * 創建文件夾接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      | POST | path={*pathToCreate*} | path->要被創建的目錄路徑 | 創建指定路徑上全部缺失文件夾<br>*路徑以```/root```開頭* | USER |
* ```/rename```
  * 創建文件夾接口
  * 參數列表
      | 方法 | 参数列表 | 参数描述 | 功能 | 用户限制 |
      | :--: | :--: | :--: | :--: | :--: |
      |POST | oldPath={*targetFileOrDirPath*}&<br>newName={*newNameOfTargetFileOrDir*} | oldPath->目標文件或文件夾路徑（包括文件或文件夾）;<br>newName->目標文件或文件夾的新名稱（不包含路徑） | 將指定位置的文件或者文件夾重命名<br>*路徑以```/root```開頭* | USER |
