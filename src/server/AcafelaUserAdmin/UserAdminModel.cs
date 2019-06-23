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

        public IList<UserInfo> getUserInfo()
        {
            IList<UserInfo> list = new List<UserInfo>();

            var userInfoList = mClient.getUserInfoList(new Empty { });
            foreach (var info in userInfoList.UserInfo)
            {
                Console.WriteLine(info.Email + "  " + info.Enabled);
                list.Add(new UserInfo
                                {
                                    Email = info.Email,
                                    Phone = info.PhoneNumber,
                                    Enabled = info.Enabled,
                                });
            }
            return list;
        }

        public int EnableUser(String email)
            => mClient.enableUser(new Email { Email_ = email }).Err;

        public int DisableUser(String email)
            => mClient.disableUser(new Email { Email_ = email }).Err;

        public int DeleteUser(String email)
            => mClient.deleteUser(new Email { Email_ = email }).Err;
    }
}
