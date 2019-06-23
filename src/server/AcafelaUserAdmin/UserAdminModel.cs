using Acafela.Rpc;
using Grpc.Core;
using harmony.usradmin.rpc;
using System;
using System.Collections.Generic;

namespace AcafelaUserAdmin
{
    public class UserAdminModel
    {
        private UserAdmin.UserAdminClient mClient;

        public UserAdminModel()
        {
            Console.WriteLine("UserAdminModel()");
            Connect();
        }

        private void Connect()
        {
            Channel channel = new Channel(
                                        "localhost:9300",
                                        ChannelCredentials.Insecure);

            mClient = new UserAdmin.UserAdminClient(channel);
        }

        public IList<harmony.usradmin.rpc.UserInfo> GetUserInfo()
            => mClient.getUserInfoList(new Empty { }).UserInfo;

        public int EnableUser(String email)
            => mClient.enableUser(new Email { Email_ = email }).Err;

        public int DisableUser(String email)
            => mClient.disableUser(new Email { Email_ = email }).Err;

        public int DeleteUser(String email)
            => mClient.deleteUser(new Email { Email_ = email }).Err;
    }
}
