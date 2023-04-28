package com.kewen.framework.auth.sys.model.resp;

import com.kewen.framework.auth.sys.model.Position;
import com.kewen.framework.auth.sys.model.Role;
import com.kewen.framework.auth.sys.model.UserDept;
import com.kewen.framework.auth.sys.mp.entity.SysUser;
import com.kewen.framework.common.core.utils.CopyObject;
import com.kewen.framework.storage.core.model.FileFillSupport;
import com.kewen.framework.storage.core.model.FileInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserResp extends SysUser implements CopyObject<SysUser>, FileFillSupport {
    protected UserDept dept;
    protected Collection<Position> positions;
    protected Collection<Role> roles;

    protected FileInfo avatar;

    @Override
    public Long getFileId() {
        return getAvatarFileId();
    }

    @Override
    public void setFileInfo(FileInfo fileInfo) {
        this.avatar = fileInfo;
    }
}