
# 说明

必须额外实现`AuthCurrentUserContextHolder`,否则Auth模块拿不到当前用户
必须额外实现`PasswordEncoder`,否则改密会有问题，直接启动报错
