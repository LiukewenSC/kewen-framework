# 说明

权限体`IAuthObject`分为两部分:
- 一部分是配置权限使用的，如`SimpleAuthObject`，
- 另一部分是用户拥有的权限，如`UserAuthInfo`

区别在于`SimpleAuthObject`是单纯的权限配置，会涉及到权限字符串的反解析，解析成权限对象，
而`UserAuthInfo`是用户自己的权限对象，其包含了自己的父级机构等，不会设计到反解析（目前的设计也反解不出来，如RELATIONDEPT__1，无法反解析获取到其是哪个部门的父部门）;
