# DownManager
> 从[Note](https://github.com/xuanu/Notes)中拆分出来的功能。
> 设计和实现一个下载的功能：**断点下载**。  
> 要想实现断点下载就必须要使用到数据库:[android-lite-orm](https://github.com/litesuits/android-lite-orm);  
### 简单的构想  
1. 一次能同时下载多少个任务。  
> 定长线程池：`ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);` 其中3为同时进行3个线程。  
2. 执行中的线程怎么做暂停  
> 执行中的线程应该是没有暂停操作的，所以在开启线程时，存入HashMap中，当暂停时，从HashMap中取出线程，设置暂停状态。其实应该也没有暂停状态，只是在这个时候跳出读Buff的操作，并结束掉当前线程。  
> 线程暂停其实就是提前结束当前线程，当继续下载时，其实是开了一个新的线程。  
3. 怎么通知界面  
> 用回调来做吧，隔2秒回调或者可以让用户填入几秒回调。  
4. 断点下载  
> 断点下载其实就是下载前看看数据库自己下载了多少，传给服务器，这样就可以少下载一点。  
5. 分块下载  
> 把大的文件分成几块来下，每一块的大小就会缩小。当下载一块的速率占不满当前网络的时候，分块会好一点。经过多次测试，稍微大一点的文件，分块下载会好一点。  
### 实现   
- 因为线程的数量在Service启动时就创建了，所以不支持动态设置。后面看看能不能在任务开始之前，用户可以先调用设置线程数，再开始任务。  
> 说一下流程吧。  
> 1. 调用DownUtils.addTask(Context,Task,DownListerer),新建一个任务。    
> 2. 在DownService#OnStartCommand中接收到任务交给DownImp#addTask()去处理，如果存在相同Url的任务会从本地取上次的下载数据。调用mExecutorService.execute(new DownRunable(DownImp,Task));  
> 3. 界面进度的监听是通过一个静态类保存的，用的是弱引用。**可能会有问题，因为我做的时候发现我界面还存在，但是弱引用已经不在了。**  
> 4. 在DownRunnable中通过文件的大小来判断要不要分块下载，15M起分。最多10个块。  
> 5. 在任务完成或者未完成，在线程的结束都会保存到数据库，且只保存一次。 任务暂停的时候直接保存数据库，并在当前线程结束时不会保存。  
### 到此一个简单的下载功能就实现了
> [Demo页面](https://github.com/xuanu/Notes/blob/master/app/src/main/java/cn/zeffect/notes/down/DownActivity.java)  
- 在setDownUrl的时候，对链接进行了编码。    
  
### 使用方法  
> 见[DownUtils.class](https://github.com/xuanu/DownManager/blob/master/downlibrary/src/main/java/utils/cn/zeffect/downlibrary/utils/DownUtils.java),更多方法，前往类中查看。
```
DownUtils#addTask(Context pContext, Task pTask);
DownUtils#addTask(Context pContext, Task pTask, DownListener pListener);
```
