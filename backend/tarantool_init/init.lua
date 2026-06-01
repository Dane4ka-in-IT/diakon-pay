box.cfg{
    listen = 3301
}

local function bootstrap()
    if not box.space.tokens then
        local tokens = box.schema.space.create('tokens', { engine = 'memtx' })
        tokens:format({
            { name = 'jti',        type = 'string'   },
            { name = 'user_id',    type = 'unsigned'  },
            { name = 'token_type', type = 'string'   },
            { name = 'expires_at', type = 'unsigned'  },
            { name = 'revoked',    type = 'boolean'  },
        })
        tokens:create_index('primary',    { type = 'hash', parts = { 'jti' } })
        tokens:create_index('by_user',    { type = 'tree', parts = { 'user_id', 'token_type' }, unique = false })
        tokens:create_index('by_expires', { type = 'tree', parts = { 'expires_at' }, unique = false })
    end

    if not box.space.otps then
        local otps = box.schema.space.create('otps', { engine = 'memtx' })
        otps:format({
            { name = 'id',         type = 'string'   },
            { name = 'email',      type = 'string'   },
            { name = 'code',       type = 'unsigned'  },
            { name = 'otp_type',   type = 'string'   },
            { name = 'expires_at', type = 'unsigned'  },
            { name = 'used',       type = 'boolean'  },
        })
        otps:create_index('primary',    { type = 'hash', parts = { 'id' } })
        otps:create_index('by_email',   { type = 'tree', parts = { 'email', 'otp_type' }, unique = false })
        otps:create_index('by_expires', { type = 'tree', parts = { 'expires_at' }, unique = false })
    end
    
    box.schema.user.passwd('admin', 'tarantool_secret')
end

box.once('bootstrap_v3', bootstrap)